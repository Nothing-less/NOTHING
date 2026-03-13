package icu.nothingless.tools;

import java.io.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import icu.nothingless.tools.config.ServiceFactoryConfig;

import java.util.function.Supplier;

public class ServiceFactory {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceFactory.class);
    
    // 缓存：接口 -> 实现类列表
    public static final Map<Class<?>, Set<Class<?>>> INTERFACE_IMPL_CACHE = new ConcurrentHashMap<>();
    
    // 单例缓存：接口 -> 实例
    private static final Map<Class<?>, Object> SINGLETON_CACHE = new ConcurrentHashMap<>();
    
    // 单例创建锁：每个接口独立锁定
    private static final Map<Class<?>, Object> SINGLETON_LOCKS = new ConcurrentHashMap<>();
    
    // 命名映射：接口 -> (实现类名 -> 实现类)
    private static final Map<Class<?>, Map<String, Class<?>>> NAMED_IMPL_CACHE = new ConcurrentHashMap<>();
    
    // 已扫描的基础包路径
    private static final Set<String> SCANNED_BASE_PACKAGES = ConcurrentHashMap.newKeySet();
    
    // 扫描锁
    private static final Object SCAN_LOCK = new Object();
    
    // 标记是否已执行过默认扫描
    private static volatile boolean defaultScanned = false;
    
    // 日志限流
    private static final Set<Class<?>> WARNED_INTERFACES = ConcurrentHashMap.newKeySet();

    // 统计已实例化的类对象
    private static final Set<Class<?>> ALL_SCANNED_CLASSES = ConcurrentHashMap.newKeySet();

    // 配置实例
    private static final ServiceFactoryConfig CONFIG = ServiceFactoryConfig.getInstance();

    public static void eagerScan() {
        ensureScanned();
        // 预加载所有单例
        for (Class<?> iface : new ArrayList<>(INTERFACE_IMPL_CACHE.keySet())) {
            try {
                getSingleton(iface);
                LOGGER.debug("预加载单例: {}", iface.getName());
            } catch (Exception e) {
                LOGGER.warn("预加载失败 {}: {}", iface.getName(), e.getMessage());
            }
        }
    }

    public static boolean isScanned() {
        return defaultScanned;
    }

    public static <T> Class<T> getImplClass(Class<T> interfaceClass) {
        List<Class<T>> impls = getImplClasses(interfaceClass);
        if (impls.isEmpty()) {
            throw new ServiceNotFoundException("No implementation found for: " + interfaceClass.getName());
        }
        if (impls.size() > 1) {
            // 每个接口只警告一次
            if (WARNED_INTERFACES.add(interfaceClass)) {
                LOGGER.warn("Multiple implementations found for " + interfaceClass.getName() + 
                              ", returning first one: " + impls.get(0).getName() + 
                              ". Consider using getImplClassByName() to specify which one to use.");
            }
        }
        return impls.get(0);
    }

    @SuppressWarnings("unchecked")
    public static <T> List<Class<T>> getImplClasses(Class<T> interfaceClass) {
        ensureScanned();
        Set<Class<?>> impls = INTERFACE_IMPL_CACHE.get(interfaceClass);
        if (impls == null || impls.isEmpty()) {
            return Collections.emptyList();
        }
        List<Class<T>> result = new ArrayList<>(impls.size());
        for (Class<?> impl : impls) {
            LOGGER.debug("Implments Class:"+impl.getName());
            result.add((Class<T>) impl);
        }
        return Collections.unmodifiableList(result);
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<T> getImplClassByName(Class<T> interfaceClass, String implName) {
        if (implName == null || implName.trim().isEmpty()) {
            throw new IllegalArgumentException("Implementation name cannot be null or empty");
        }
        ensureScanned();
        Map<String, Class<?>> namedMap = NAMED_IMPL_CACHE.get(interfaceClass);
        if (namedMap == null) {
            throw new ServiceNotFoundException("No implementations found for: " + interfaceClass.getName());
        }
        Class<?> impl = namedMap.get(implName);
        if (impl == null) {
            throw new ServiceNotFoundException("Implementation '" + implName + "' not found for: " + interfaceClass.getName());
        }
        return (Class<T>) impl;
    }

    public static <T> T createInstance(Class<T> interfaceClass) {
        Class<T> implClass = getImplClass(interfaceClass);
        return newInstance(implClass);
    }

    public static <T> T createInstance(Class<T> interfaceClass, String implName) {
        Class<T> implClass = getImplClassByName(interfaceClass, implName);
        return newInstance(implClass);
    }

    /**
     * 获取单例实例
     */
    @SuppressWarnings("unchecked")
    public static <T> T getSingleton(Class<T> interfaceClass) {
        ensureScanned();
        
        // 已存在则直接返回
        Object existing = SINGLETON_CACHE.get(interfaceClass);
        if (existing != null) {
            return (T) existing;
        }
        
        // 获取或创建该接口的专用锁对象
        Object lock = SINGLETON_LOCKS.computeIfAbsent(interfaceClass, k -> new Object());
        
        synchronized (lock) {
            // 双重检查（在锁内）
            Object singleton = SINGLETON_CACHE.get(interfaceClass);
            if (singleton == null) {
                Class<T> implClass = getImplClass(interfaceClass);
                singleton = newInstance(implClass);
                SINGLETON_CACHE.put(interfaceClass, singleton);
            }
            return (T) singleton;
        }
    }

    /**
     * 获取单例实例
     * 
     * @param interfaceClass 接口类型
     * @param supplier 自定义实例创建逻辑
     * @return 单例实例
     */
    @SuppressWarnings("unchecked")
    public static <T> T getSingleton(Class<T> interfaceClass, Supplier<T> supplier) {
        if (supplier == null) {
            throw new IllegalArgumentException("Supplier cannot be null");
        }
        ensureScanned();
        // 快速路径
        Object existing = SINGLETON_CACHE.get(interfaceClass);
        if (existing != null) {
            return (T) existing;
        }
        Object lock = SINGLETON_LOCKS.computeIfAbsent(interfaceClass, k -> new Object());
        synchronized (lock) {
            Object singleton = SINGLETON_CACHE.get(interfaceClass);
            if (singleton == null) {
                try {
                    singleton = supplier.get();
                    if (singleton == null) {
                        throw new ServiceFactoryException("Supplier returned null for: " + interfaceClass.getName(), null);
                    }
                    SINGLETON_CACHE.put(interfaceClass, singleton);
                } catch (Exception e) {
                    throw new ServiceFactoryException("Failed to create singleton via supplier for: " + interfaceClass.getName(), e);
                }
            }
            return (T) singleton;
        }
    }

    /**
     * 清除指定接口的单例缓存（支持热替换）
     * 同时清理对应的锁对象，防止内存泄漏
     */
    public static <T> void clearSingleton(Class<T> interfaceClass) {
        SINGLETON_CACHE.remove(interfaceClass);
        SINGLETON_LOCKS.remove(interfaceClass);
    }

    /**
     * 清除所有单例缓存和锁对象
     */
    public static void clearAllSingletons() {
        SINGLETON_CACHE.clear();
        SINGLETON_LOCKS.clear();
    }

    /**
     * 扫描包
     */
    public static void scanPackage(String basePackage) {
        if (basePackage == null || basePackage.trim().isEmpty()) {
            throw new IllegalArgumentException("Base package cannot be null or empty");
        }
        
        // 快速路径
        if (SCANNED_BASE_PACKAGES.contains(basePackage)) {
            return;
        }
        
        synchronized (SCAN_LOCK) {
            // 双重检查
            if (SCANNED_BASE_PACKAGES.contains(basePackage)) {
                return;
            }
            doScanPackage(basePackage);
            SCANNED_BASE_PACKAGES.add(basePackage);
        }
    }

    private static void ensureScanned() {
        if (!defaultScanned) {
            synchronized (SCAN_LOCK) {
                if (!defaultScanned) {
                    // 优先级 1：如果配置中有包路径，使用配置
                    if (CONFIG.hasPackages()) {
                        for (String pkg : CONFIG.getScanPackages()) {
                            if (!isExcluded(pkg)) {
                                doScanPackage(pkg);
                                SCANNED_BASE_PACKAGES.add(pkg);
                            }
                        }
                    } else {
                        // 优先级 2：回退到默认包（当前类所在包）
                        String defaultPackage = ServiceFactory.class.getPackage().getName();
                        doScanPackage(defaultPackage);
                        SCANNED_BASE_PACKAGES.add(defaultPackage);
                    }
                    defaultScanned = true;
                }
            }
        }
    }

    private static boolean isExcluded(String packageName) {
        for (String exclude : CONFIG.getExcludePackages()) {
            if (packageName.startsWith(exclude)) return true;
        }
        return false;
    }

    // 重新加载配置并重新扫描（热更新支持）
    public static void reloadConfigAndRescan() {
        synchronized (SCAN_LOCK) {
            // 清空缓存
            INTERFACE_IMPL_CACHE.clear();
            NAMED_IMPL_CACHE.clear();
            SINGLETON_CACHE.clear();
            SINGLETON_LOCKS.clear();
            SCANNED_BASE_PACKAGES.clear();
            ALL_SCANNED_CLASSES.clear();
            defaultScanned = false;
            
            // 强制重新加载配置
            // ServiceFactoryConfig.reload(); // 需要实现 reload 方法

            // 重新扫描
            ensureScanned();
        }
    }

    private static void doScanPackage(String basePackage) {
        String packagePath = basePackage.replace(".", "/");
        ClassLoader classLoader = getClassLoader();
        
        try {
            Enumeration<URL> resources = classLoader.getResources(packagePath);
            if (!resources.hasMoreElements()) {
                LOGGER.warn("No resources found for package: " + basePackage);
                return;
            }
            
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                String protocol = url.getProtocol();
                
                if ("file".equals(protocol)) {
                    String filePath = URLDecoder.decode(url.getFile(), StandardCharsets.UTF_8);
                    File dir = new File(filePath);
                    if (dir.exists() && dir.isDirectory()) {
                        scanDirectory(dir, basePackage, classLoader);
                    }
                } else if ("jar".equals(protocol)) {
                    scanJar(url, basePackage, classLoader);
                }
            }
        } catch (IOException e) {
            throw new ServiceFactoryException("Failed to scan package: " + basePackage, e);
        }
    }

    private static void scanDirectory(File dir, String packageName, ClassLoader classLoader) {
        File[] files = dir.listFiles();
        if (files == null) return;
        
        for (File file : files) {
            String fileName = file.getName();
            
            if (file.isDirectory()) {
                String subPackage = packageName + "." + fileName;
                scanDirectory(file, subPackage, classLoader);
            } else if (fileName.endsWith(".class")) {
                String className = packageName + "." + fileName.substring(0, fileName.length() - 6);
                loadClass(className, classLoader);
            }
        }
    }

    private static void scanJar(URL url, String basePackage, ClassLoader classLoader) {
        String packagePath = basePackage.replace(".", "/");
        
        try {
            JarURLConnection jarConn = (JarURLConnection) url.openConnection();
            try (JarFile jarFile = jarConn.getJarFile()) {
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String entryName = entry.getName();
                    
                    // 检查是否是目标包下的类文件
                    if (entryName.startsWith(packagePath) && 
                        entryName.endsWith(".class") && 
                        !entry.isDirectory()) {
                        // 转换为类名
                        String className = entryName.replace("/", ".")
                                                   .substring(0, entryName.length() - 6);
                        loadClass(className, classLoader);
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to scan JAR: " + url, e);
        }
    }

    private static void loadClass(String className, ClassLoader classLoader) {
        try {
            LOGGER.debug("尝试加载类: " + className);
            // Class<?> clazz = Class.forName(className, false, classLoader);
            Class<?> clazz;
            try {
                ClassLoader effectiveLoader = getClassLoader();
                LOGGER.debug("尝试加载类: " + className + " using " + effectiveLoader);
                clazz = Class.forName(className, false, effectiveLoader);
            } catch (Exception e) {
                // 回退到传入的类加载器
                clazz = Class.forName(className, false, classLoader);
            }


            // 跳过非实现类
            if (clazz.isInterface() || clazz.isAnnotation() || 
                clazz.isEnum() || java.lang.reflect.Modifier.isAbstract(clazz.getModifiers())) {
                LOGGER.debug("  -> 被过滤（接口/抽象类/枚举）:  " + className);
                return;
            }
            LOGGER.debug("  -> 有效实现类: " + className);
            
            // 收集所有实现的接口（包括父类），提前过滤标记接口
            Set<Class<?>> interfaces = new HashSet<>();
            collectInterfaces(clazz, interfaces);
            ALL_SCANNED_CLASSES.add(clazz);
            
            for (Class<?> iface : interfaces) {
                cacheImplementation(iface, clazz);
            }
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            LOGGER.error( "Failed to load class: " + className, e);
        } catch (Exception e) {
            LOGGER.error( "Unexpected error loading class: " + className, e);
        }
    }

    private static void collectInterfaces(Class<?> clazz, Set<Class<?>> interfaces) {
        // 直接实现的接口
        for (Class<?> iface : clazz.getInterfaces()) {
            // 提前剪枝：跳过标记接口及其所有父接口
            if (isMarkerInterface(iface)) {
                continue;
            }
            interfaces.add(iface);
            collectParentInterfaces(iface, interfaces);
        }
        
        // 父类的接口
        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null && superClass != Object.class) {
            collectInterfaces(superClass, interfaces);
        }
    }

    private static void collectParentInterfaces(Class<?> iface, Set<Class<?>> interfaces) {
        for (Class<?> parent : iface.getInterfaces()) {
            // 提前过滤
            if (isMarkerInterface(parent)) {
                continue;
            }
            interfaces.add(parent);
            collectParentInterfaces(parent, interfaces);
        }
    }

    /**
     * 判断是否为标记接口
     */
    private static boolean isMarkerInterface(Class<?> iface) {
        return iface == java.io.Serializable.class || 
               iface == java.lang.Cloneable.class ||
               iface == java.io.Externalizable.class;
    }

    /**
     * 缓存实现类
     */
    private static void cacheImplementation(Class<?> interfaceClass, Class<?> implClass) {
        INTERFACE_IMPL_CACHE.compute(interfaceClass, (k, existing) -> {
            Set<Class<?>> set = existing != null 
                ? new LinkedHashSet<>(existing)
                : new LinkedHashSet<>();
            
            boolean added = set.add(implClass);  // O(1) 查重+添加
            if (added) {
                LOGGER.debug("Add one implClass: " + implClass.getName());
            }
            return set;
        });
        
        NAMED_IMPL_CACHE.compute(interfaceClass, (k, existingMap) -> {
            Map<String, Class<?>> map = existingMap != null ? 
                new ConcurrentHashMap<>(existingMap) : new ConcurrentHashMap<>();
            
            String simpleName = implClass.getSimpleName();
            
            //  跳过匿名内部类和局部类（简单类名为空）
            if (simpleName == null || simpleName.isEmpty()) {
                // 仍然缓存全限定名，但不生成 beanName
                map.put(implClass.getName(), implClass);
                return map;
            }
            
            // 生成 beanName
            String beanName = simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);
            
            map.put(beanName, implClass);
            map.put(implClass.getName(), implClass);
            map.put(simpleName, implClass);
            
            return map;
        });
    }

    private static ClassLoader getClassLoader() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = ServiceFactory.class.getClassLoader();
        }
        if (cl == null) {
            cl = ClassLoader.getSystemClassLoader();
        }
        return cl;
    }

    /**
     * 创建实例
     */
    private static <T> T newInstance(Class<T> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException e) {
            throw new ServiceFactoryException(
                "No default constructor found for: " + clazz.getName() + 
                ". Ensure the class has a public no-arg constructor.", e);
        } catch (InstantiationException e) {
            throw new ServiceFactoryException(
                "Cannot instantiate abstract class or interface: " + clazz.getName(), e);
        } catch (IllegalAccessException e) {
            throw new ServiceFactoryException(
                "Constructor is not accessible for: " + clazz.getName() + 
                ". Ensure the constructor is public.", e);
        } catch (Exception e) {
            throw new ServiceFactoryException(
                "Failed to create instance of: " + clazz.getName() + 
                ". Cause: " + e.getMessage(), e);
        }
    }
    public static Set<Class<?>> getAllScannedClasses() {
        ensureScanned();
        return Collections.unmodifiableSet(ALL_SCANNED_CLASSES);
    }
    // 获取所有实现类（包括所有接口的实现）
    @SuppressWarnings("unchecked")
    public static <T> List<Class<? extends T>> getAllImplementations(Class<T> type) {
        ensureScanned();
        List<Class<? extends T>> result = new ArrayList<>();
        for (Class<?> clazz : ALL_SCANNED_CLASSES) {
            if (type.isAssignableFrom(clazz)) {  // 检查 type 是否是 clazz 的父类/接口
                result.add((Class<? extends T>) clazz);
            }
        }
        return result;
    }

    public static class ServiceNotFoundException extends RuntimeException {
        public ServiceNotFoundException(String message) {
            super(message);
        }
    }

    public static class ServiceFactoryException extends RuntimeException {
        public ServiceFactoryException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
}
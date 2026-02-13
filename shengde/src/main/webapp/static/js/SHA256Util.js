/**
 * SHA256 加密工具类
 * 前后端共用算法，确保生成一致的密文
 */

const SHA256Util = {
    /**
     * 对字符串进行 SHA256 加密
     * @param {string} message - 原始字符串
     * @returns {string} - 64位十六进制密文（小写）
     */
    encrypt: function(message) {
        if (!message) return null;
        
        // 将字符串转换为 UTF-8 编码的字节数组
        function utf8Encode(str) {
            return new TextEncoder().encode(str);
        }
        
        // 核心 SHA256 实现
        function sha256Bytes(bytes) {
            const K = [
                0x428a2f98, 0x71374491, 0xb5c0fbcf, 0xe9b5dba5,
                0x3956c25b, 0x59f111f1, 0x923f82a4, 0xab1c5ed5,
                0xd807aa98, 0x12835b01, 0x243185be, 0x550c7dc3,
                0x72be5d74, 0x80deb1fe, 0x9bdc06a7, 0xc19bf174,
                0xe49b69c1, 0xefbe4786, 0x0fc19dc6, 0x240ca1cc,
                0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da,
                0x983e5152, 0xa831c66d, 0xb00327c8, 0xbf597fc7,
                0xc6e00bf3, 0xd5a79147, 0x06ca6351, 0x14292967,
                0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13,
                0x650a7354, 0x766a0abb, 0x81c2c92e, 0x92722c85,
                0xa2bfe8a1, 0xa81a664b, 0xc24b8b70, 0xc76c51a3,
                0xd192e819, 0xd6990624, 0xf40e3585, 0x106aa070,
                0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5,
                0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3,
                0x748f82ee, 0x78a5636f, 0x84c87814, 0x8cc70208,
                0x90befffa, 0xa4506ceb, 0xbef9a3f7, 0xc67178f2
            ];
            
            let h0 = 0x6a09e667, h1 = 0xbb67ae85, h2 = 0x3c6ef372, h3 = 0xa54ff53a;
            let h4 = 0x510e527f, h5 = 0x9b05688c, h6 = 0x1f83d9ab, h7 = 0x5be0cd19;
            
            const data = new Uint8Array(bytes);
            const bitLen = data.length * 8;
            
            // 填充
            const padLen = (data.length % 64 < 56) ? (56 - data.length % 64) : (120 - data.length % 64);
            const totalLen = data.length + padLen + 8;
            const msg = new Uint8Array(totalLen);
            msg.set(data);
            msg[data.length] = 0x80;
            
            // 附加长度（大端序）
            const view = new DataView(msg.buffer);
            view.setUint32(totalLen - 4, bitLen, false);
            
            // 处理每个 64 字节块
            for (let i = 0; i < totalLen; i += 64) {
                const w = new Uint32Array(64);
                for (let t = 0; t < 16; t++) {
                    w[t] = view.getUint32(i + t * 4, false);
                }
                for (let t = 16; t < 64; t++) {
                    const s0 = ((w[t-15] >>> 7) | (w[t-15] << 25)) ^ ((w[t-15] >>> 18) | (w[t-15] << 14)) ^ (w[t-15] >>> 3);
                    const s1 = ((w[t-2] >>> 17) | (w[t-2] << 15)) ^ ((w[t-2] >>> 19) | (w[t-2] << 13)) ^ (w[t-2] >>> 10);
                    w[t] = (w[t-16] + s0 + w[t-7] + s1) >>> 0;
                }
                
                let a = h0, b = h1, c = h2, d = h3, e = h4, f = h5, g = h6, h = h7;
                
                for (let t = 0; t < 64; t++) {
                    const S1 = ((e >>> 6) | (e << 26)) ^ ((e >>> 11) | (e << 21)) ^ ((e >>> 25) | (e << 7));
                    const ch = (e & f) ^ (~e & g);
                    const temp1 = (h + S1 + ch + K[t] + w[t]) >>> 0;
                    const S0 = ((a >>> 2) | (a << 30)) ^ ((a >>> 13) | (a << 19)) ^ ((a >>> 22) | (a << 10));
                    const maj = (a & b) ^ (a & c) ^ (b & c);
                    const temp2 = (S0 + maj) >>> 0;
                    
                    h = g; g = f; f = e; e = (d + temp1) >>> 0;
                    d = c; c = b; b = a; a = (temp1 + temp2) >>> 0;
                }
                
                h0 = (h0 + a) >>> 0; h1 = (h1 + b) >>> 0; h2 = (h2 + c) >>> 0; h3 = (h3 + d) >>> 0;
                h4 = (h4 + e) >>> 0; h5 = (h5 + f) >>> 0; h6 = (h6 + g) >>> 0; h7 = (h7 + h) >>> 0;
            }
            
            // 转换为十六进制字符串
            const toHex = (n) => n.toString(16).padStart(8, '0');
            return toHex(h0) + toHex(h1) + toHex(h2) + toHex(h3) + 
                   toHex(h4) + toHex(h5) + toHex(h6) + toHex(h7);
        }
        
        return sha256Bytes(utf8Encode(message));
    },

    /**
     * 带盐值的加密（增强安全性）
     * @param {string} message - 原始字符串
     * @param {string} salt - 盐值
     * @returns {string} - 加密后的密文
     */
    encryptWithSalt: function(message, salt) {
        if (!salt) {
            return this.encrypt(message);
        }
        return this.encrypt(message + salt);
    },

    /**
     * 表单加密辅助方法
     * 自动将明文密码加密并填入隐藏字段
     * 
     * @param {string} plainFieldId - 明文密码输入框ID
     * @param {string} hiddenFieldId - 隐藏密文输入框ID
     * @param {string} salt - 可选的盐值
     * @returns {boolean} - 是否加密成功
     */
    encryptFormField: function(plainFieldId, hiddenFieldId, salt) {
        const plainInput = document.getElementById(plainFieldId);
        const hiddenInput = document.getElementById(hiddenFieldId);
        
        if (!plainInput || !hiddenInput) {
            console.error('找不到指定的输入框元素');
            return false;
        }
        
        const plainPassword = plainInput.value;
        if (!plainPassword) {
            alert('请输入密码');
            return false;
        }
        
        // 加密（可选加盐）
        const encrypted = salt ? this.encryptWithSalt(plainPassword, salt) 
                               : this.encrypt(plainPassword);
        
        // 设置密文到隐藏字段
        hiddenInput.value = encrypted;
        
        // 可选：清空或禁用明文输入框（增加安全性）
        plainInput.value = '';
        // 或者：plainInput.disabled = true;
        
        console.log('密码已加密：', encrypted);
        return true;
    }
};
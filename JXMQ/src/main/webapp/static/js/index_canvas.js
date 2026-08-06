const canvas = document.createElement('canvas');
const ctx = canvas.getContext('2d');
document.body.prepend(canvas);

function resize() {
  canvas.width = innerWidth;
  canvas.height = innerHeight;
}
window.addEventListener('resize', resize);
resize();

// 颜色插值工具
function lerpColor(a, b, t) {
  const ar = parseInt(a.slice(1,3),16), ag = parseInt(a.slice(3,5),16), ab = parseInt(a.slice(5,7),16);
  const br = parseInt(b.slice(1,3),16), bg = parseInt(b.slice(3,5),16), bb = parseInt(b.slice(5,7),16);
  const r = Math.round(ar + (br - ar) * t);
  const g = Math.round(ag + (bg - ag) * t);
  const b = Math.round(ab + (bb - ab) * t);
  return `rgb(${r},${g},${b})`;
}

const palette = ['#ffd1dc', '#c9e9f6', '#b5e7a0', '#fff4e6', '#e6e6fa'];
let t = 0;

function draw() {
  t += 0.002;  // 速度
  
  const idx = Math.floor(t) % palette.length;
  const next = (idx + 1) % palette.length;
  const phase = t - Math.floor(t);
  
  const c1 = lerpColor(palette[idx], palette[next], phase);
  const c2 = lerpColor(palette[(idx+2)%palette.length], palette[(idx+3)%palette.length], phase);
  
  const grad = ctx.createLinearGradient(0, 0, canvas.width, canvas.height);
  grad.addColorStop(0, c1);
  grad.addColorStop(1, c2);
  
  ctx.fillStyle = grad;
  ctx.fillRect(0, 0, canvas.width, canvas.height);
  
  requestAnimationFrame(draw);
}
draw();
precision highp float;
attribute vec3 aPosition;
attribute vec3 aNormal;
attribute vec2 aCell;
uniform mat4 uVP, uLightVP;
varying vec4 vShadow;
uniform float uTime, uScale, uDepth;
varying vec3 vPosition, vNormal;
varying float vLocalZ;
void main() {
  vec2 center = aCell * uScale;
  float wave = sin(aCell.x * .43 + uTime) * .34 + cos(aCell.y * .38 - uTime * .7) * .28;
  wave += sin(length(aCell) * .58 - uTime * .83) * .23;
  float height = .24 + uDepth * (.9 + wave);
  vec3 p = vec3(center + aPosition.xy * uScale * .94, (aPosition.z + .5) * height);
  vec3 n = normalize(aNormal / vec3(uScale * .94, uScale * .94, height));
  vPosition = p;
  vNormal = n;
  vLocalZ = aPosition.z + .5;
  vShadow = uLightVP * vec4(p, 1.);
  gl_Position = uVP * vec4(p, 1.);
}

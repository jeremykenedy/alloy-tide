#ifdef GL_FRAGMENT_PRECISION_HIGH
precision highp float;
#else
precision mediump float;
#endif
uniform vec3 uEye, uMetal, uAccent;
uniform sampler2D uShadow, uEnvironment;
varying vec4 vShadow;
uniform float uRoughness, uBrightness, uNeon, uTileScale, uGlowPhase;
varying vec3 vPosition, vNormal;
varying float vLocalZ;
const float PI = 3.14159265;
vec3 environment(vec3 r) {
  vec2 uv = vec2(atan(r.y,r.x)/(2.*PI)+.5,.5-asin(clamp(r.z,-1.,1.))/PI);
  vec4 radiance = texture2D(uEnvironment,uv,uRoughness*4.);
  return radiance.rgb*radiance.a*11.2 + vec3(.25,.28,.32);
}
float visibility(vec2 uv, float depth) {
  vec2 encoded = texture2D(uShadow,uv).rg;
  return smoothstep(depth-.0025,depth-.001, dot(encoded,vec2(1.,1./255.)));
}
void main() {
  vec3 N = normalize(vNormal), V = normalize(uEye-vPosition);
  vec3 L = normalize(vec3(-.5,-.3,1.)), H = normalize(L+V);
  float nv = max(dot(N,V),.001), nl = max(dot(N,L),0.), nh = max(dot(N,H),0.);
  float rough = uRoughness, alpha = rough*rough;
  float denominator = nh*nh*(alpha*alpha-1.)+1.;
  float D = alpha*alpha/(PI*denominator*denominator+.001);
  float k = (rough+1.)*(rough+1.)/8.;
  float G = nv/(nv*(1.-k)+k)*nl/(nl*(1.-k)+k);
  vec3 metal = mix(uMetal,vec3(.018,.024,.03),uNeon*.94);
  vec3 F = metal+(1.-metal)*pow(1.-max(dot(H,V),0.),5.);
  vec3 reflected = environment(reflect(-V,N));
  vec3 color = reflected*(metal+(1.-metal)*pow(1.-nv,5.))*(.64-.2*rough);
  color += D*G*F/(4.*nv*max(nl,.001)+.001)*nl*vec3(1.2,1.25,1.3);
  float occlusion = mix(.38,1.,smoothstep(0.,.35,vLocalZ));
  vec3 shadow = vShadow.xyz/vShadow.w*.5+.5;
  float d = 1./2048.;
  float lit = .25*(visibility(shadow.xy+vec2(-d,-d),shadow.z)+visibility(shadow.xy+vec2(d,-d),shadow.z)+visibility(shadow.xy+vec2(-d,d),shadow.z)+visibility(shadow.xy+vec2(d,d),shadow.z));
  color *= occlusion*mix(.24,1.,lit);
  float edge = mix(pow(1.-abs(N.z),3.) * exp(-vLocalZ*3.5), pow(1.-abs(N.z),.8)*pow(vLocalZ,10.),uNeon);
  vec2 cell = floor(vPosition.xy/uTileScale+.5);
  vec2 local = abs(vPosition.xy/uTileScale-cell);
  float seam = exp(-max(0.,.47-max(local.x,local.y))*65.);
  float active = smoothstep(-.25,.55,sin(cell.x*.71+cell.y*.93+uGlowPhase));
  color *= mix(1.,.6,uNeon);
  color += uAccent*seam*active*uNeon*2.5;
  color += uAccent*edge*mix(.16,7.,uNeon);
  color += uAccent*.025*(1.-max(N.z,0.));
  color *= uBrightness;
  color = (color*(2.51*color+.03))/(color*(2.43*color+.59)+.14);
  gl_FragColor = vec4(pow(clamp(color,0.,1.),vec3(1./2.2)),1.);
}

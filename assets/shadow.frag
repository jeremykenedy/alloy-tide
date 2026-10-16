precision mediump float;
void main() {
  vec2 depth = fract(gl_FragCoord.z * vec2(1.,255.));
  depth.x -= depth.y/255.;
  gl_FragColor = vec4(depth,0.,1.);
}

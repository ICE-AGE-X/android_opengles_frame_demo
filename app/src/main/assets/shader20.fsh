varying vec2 outTexCoord;
uniform sampler2D texture1;
varying vec4 outPos;
void main()
{
    vec2 offset=vec2(1.0f,0.5f);
    vec2 uv=outTexCoord*offset;
    vec4 color =texture2D(texture1,vec2(uv.x,uv.y));

    vec2 aUv=uv+vec2(0.0,0.5);
    color.a=texture2D(texture1,vec2(aUv.x,aUv.y)).r;
    gl_FragColor=color;
}
#version 310 es
precision lowp float;
out vec4 FragColor;
in vec3 ourColor;
in vec2 TexCoord;
uniform sampler2D texture1;
void main()
{
    FragColor=texture(texture1,vec2(TexCoord.x,TexCoord.y));
}

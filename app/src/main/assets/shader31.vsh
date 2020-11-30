#version 310 es
precision lowp float;
layout(location=0) in vec3 aPos;
layout(location=1) in vec3 aColor;
layout(location=2) in vec2 aTexCoord;
out vec3 ourColor;
out vec2 TexCoord;
uniform mat4 projection;

void main()
{
    gl_Position=projection * vec4(aPos.x,aPos.y,aPos.z,1.0f);
    ourColor=aColor;
    TexCoord=aTexCoord;
}
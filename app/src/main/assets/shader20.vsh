attribute vec3 aPos;
attribute vec2 texCoord;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

varying vec2 outTexCoord;
varying vec4 outPos;
void main()
{
    outTexCoord=texCoord;
    gl_Position=projection *view *model* vec4(aPos.x,aPos.y*-1.0f,aPos.z,1.0f);
}


#version 430

in vec2 tes_out;
out vec4 color;
uniform mat4 mvp;

layout (binding = 1) uniform sampler2D tex_color;
layout (binding = 4) uniform sampler2D tex_height;

void main(void)
{	color = texture(tex_color, tes_out);
}
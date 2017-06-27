#version 430

layout (vertices = 4) out;

in vec2 tc[];
out vec2 tcs_out[];

uniform mat4 mvp;
layout (binding=1) uniform sampler2D tex_color;
layout (binding = 4) uniform sampler2D tex_height;

void main(void)
{	int TL = 32;
	if (gl_InvocationID == 0)
	{ 	vec4 p0 = normalize(mvp * gl_in[0].gl_Position);
		vec4 p1 = normalize(mvp * gl_in[1].gl_Position);
		vec4 p2 = normalize(mvp * gl_in[2].gl_Position);
		vec4 p3 = normalize(mvp * gl_in[3].gl_Position);
		float width  = length(p2.xy - p0.xy) * 16.0 + 1.0;
		float height = length(p1.xy - p0.xy) * 16.0 + 1.0;
		gl_TessLevelOuter[0] = height;
		gl_TessLevelOuter[1] = width;
		gl_TessLevelOuter[2] = height;
		gl_TessLevelOuter[3] = width;
		gl_TessLevelInner[0] = width;
		gl_TessLevelInner[1] = height;
	}
	
	tcs_out[gl_InvocationID] = tc[gl_InvocationID];
	gl_out[gl_InvocationID].gl_Position = gl_in[gl_InvocationID].gl_Position;
}
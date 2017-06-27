#version 430

layout (quads, fractional_even_spacing, ccw) in;

uniform mat4 mvp;
layout (binding = 1) uniform sampler2D tex_color;
layout (binding = 4) uniform sampler2D tex_height;

in vec2 tcs_out[];
out vec2 tes_out;

void main (void)
{	vec2 tc = vec2(tcs_out[0].x + (gl_TessCoord.x)/64,
					tcs_out[0].y + (1.0-gl_TessCoord.y)/64);

	// map the tessellated grid onto the texture rectangle:
	vec4 tessellatedPoint = vec4(gl_in[0].gl_Position.x + gl_TessCoord.x/64, 0.0,
								gl_in[0].gl_Position.z + gl_TessCoord.y/64, 1.0);
	
	// add the height from the height map to the vertex:
	tessellatedPoint.y += (texture(tex_height, tc).r) / 50.0;
	
	gl_Position = mvp * tessellatedPoint;
	tes_out = tc;	
}
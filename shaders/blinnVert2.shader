#version 430

layout (location=0) in vec3 vertPos;
layout (location=1) in vec3 vertNormal;
//layout (location=2) in vec2 vertTex;
out vec3 vNormal, vLightDir, vVertPos, vHalfVec; 
out vec4 shadow_coord;
out vec3 originalVertex;
out vec3 originalPosition;
out vec3 pos_eye;
out vec3 normal_eye;
//out vec2 tc;

struct PositionalLight
{	vec4 ambient, diffuse, specular;
	vec3 position;
};
struct Material
{	vec4 ambient, diffuse, specular;   
	float shininess;
};

uniform vec4 globalAmbient;
uniform PositionalLight light;
uniform Material material;
uniform mat4 mv_matrix;
uniform mat4 proj_matrix;
uniform mat4 v_matrix;
uniform mat4 m_matrix;
uniform mat4 normalMat;
uniform mat4 texRot;
uniform mat4 shadowMVP;
uniform float use_bumpMap;
uniform float noise_use;
uniform int flipNormal;
layout (binding=0) uniform sampler2DShadow shadowTex;
layout (binding=2) uniform samplerCube t;
layout (binding=3) uniform sampler3D ss;

vec4 clip_plane = vec4(1.0, 0.0, 0.0, 0.3);

void main(void)
{	//output the vertex position to the rasterizer for interpolation
	vVertPos = (mv_matrix * vec4(vertPos,1.0)).xyz;
        
	//get a vector from the vertex to the light and output it to the rasterizer for interpolation
	vLightDir = light.position - vVertPos;

	//get a vertex normal vector in eye space and output it to the rasterizer for interpolation
	vNormal = (normalMat * vec4(vertNormal,1.0)).xyz;
	
	if(flipNormal == 1)
	{
		vNormal = -vNormal;
	}
	
	// calculate the half vector (L+V)
	vHalfVec = (vLightDir-vVertPos).xyz;
	
	gl_ClipDistance[0] = dot(clip_plane.xyz, vertPos) + clip_plane.w;

	pos_eye = vec3(m_matrix * v_matrix * vec4(vertPos,1.0));
	normal_eye = vec3(m_matrix * v_matrix * vec4(vertNormal,0.0));
	shadow_coord = shadowMVP * vec4(vertPos,1.0);
	originalVertex = vertPos;
	//originalPosition = vertPos;
	originalPosition = vec3(texRot * vec4(vertPos,1.0)).xyz;
	gl_Position = proj_matrix * mv_matrix * vec4(vertPos,1.0);
	//tc = vertTex;
}

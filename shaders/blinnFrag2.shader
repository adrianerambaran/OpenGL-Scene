#version 430

in vec3 vNormal, vLightDir, vVertPos, vHalfVec;
in vec4 shadow_coord;
//in vec2 tc;
in vec3 originalVertex;
in vec3 originalPosition;
in vec3 pos_eye;
in vec3 normal_eye;
out vec4 fragColor;

 
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
uniform mat4 v_matrix;
uniform mat4 m_matrix; 
uniform mat4 proj_matrix;
uniform mat4 normalMat;
uniform mat4 shadowMVP;
uniform mat4 texRot;
uniform float use_tex;
uniform float use_bump;
uniform float reflect_tex;
uniform float noise_use;
layout (binding=0) uniform sampler2DShadow shadowTex;
layout (binding=1) uniform sampler2D s;
layout (binding=2) uniform samplerCube t;
layout (binding=3) uniform sampler3D ss;

void main(void)
{	vec3 L = normalize(vLightDir);
	vec3 N = normalize(vNormal);
	vec3 V = normalize(-vVertPos);
	vec3 H = normalize(vHalfVec);
	//vec3 r = reflect(normalize(-vVertPos), normalize(vNormal));
	vec3 Ie = normalize(pos_eye);
	vec3 normalEye = normalize(normal_eye);
	vec3 r = reflect(Ie, normalEye);
	vec4 texColor = texture(ss,originalPosition/2.0+0.5);
	vec4 matAmb = texColor;
	vec4 matDiff = texColor;
	vec4 matSpec = texColor;
	if(use_bump != 0)
	{
		float a = 0.25;
		float b = 100.0;
		float x = originalVertex.x;
		float y = originalVertex.y;
		float z = originalVertex.z;
		N.x = vNormal.x + a*sin(b*x);
		N.y = vNormal.y + a*sin(b*y);
		N.z = vNormal.z + a*sin(b*z);
		N = normalize(N);
	}
	float inShadow = textureProj(shadowTex, shadow_coord);

	if(reflect_tex == 0)
	{
		if(noise_use == 0)
		{
			fragColor = globalAmbient * material.ambient
				+ light.ambient * material.ambient;

			if (inShadow != 0.0)
			{	
				fragColor += light.diffuse * material.diffuse * max(dot(L,N),0.0)
					+ light.specular * material.specular
					* pow(max(dot(H,N),0.0),material.shininess*3.0);
			}
		}
		else if(noise_use == 1)
		{
			//fragColor = globalAmbient * material.ambient + light.ambient * material.ambient;
			if (inShadow != 0.0)
			{
				fragColor = globalAmbient * matAmb + light.ambient * matAmb+ light.diffuse * matDiff  
					* max(dot(L,N),0.0) + light.specular * matSpec * pow(max(dot(H,N),0.0),material.shininess) * 0.3;
				//fragColor = texColor;
			}
		}
	}
	else if(reflect_tex == 1)
	{
		r = vec3(inverse(v_matrix) * vec4(r,0.0));
		fragColor = texture(t,r);
	}


	

}

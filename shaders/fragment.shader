#version 430

in vec3 varyingNormal;
in vec3 varyingLightDir;
in vec3 varyingVertPos;
in vec3 varyingHalfVector;
in vec2 tc;
out vec4 fragColor;

struct PositionalLight
{	vec4 ambient;  
	vec4 diffuse;  
	vec4 specular;  
	vec3 position;
};

struct Material
{	vec4 ambient;  
	vec4 diffuse;  
	vec4 specular;  
	float shininess;
};

uniform vec4 globalAmbient;
uniform PositionalLight light;
uniform Material material;
uniform mat4 mv_matrix;	 
uniform mat4 proj_matrix;
uniform mat4 norm_matrix;
uniform vec4 color;
uniform mat4 shadowMVP;
uniform float use_tex;
layout (binding=1) uniform sampler2D s;


void main(void)
{	// normalize the light, normal, and view vectors:
	vec3 L = normalize(varyingLightDir);
	vec3 N = normalize(varyingNormal);
	vec3 V = normalize(-varyingVertPos);
	vec3 H = varyingHalfVector;

	

	// get the angle between the light and surface normal:
	float cosTheta = dot(L,N);
	
	
	// get angle between the normal and the halfway vector
	float cosPhi = dot(H,N);

	vec4 texColor = texture(s, tc);

	// compute ADS contributions (per pixel):
	vec3 ambient = ((globalAmbient * material.ambient) + (light.ambient * material.ambient)).xyz;
	vec3 diffuse = light.diffuse.xyz * material.diffuse.xyz * max(cosTheta,0.0);
	vec3 specular = light.specular.xyz * material.specular.xyz * pow(max(cosPhi,0.0), material.shininess*3.0);


	
	if(color.w == 1)
	{
		fragColor = color;
	}
	else if(use_tex != 0)
	{
		
		//fragColor = vec4((ambient + diffuse), 1) * texColor + vec4(specular, 1.0);
		fragColor = globalAmbient * material.ambient + light.ambient * material.ambient;
		fragColor +=vec4((ambient + diffuse), 1) * texColor + vec4(specular, 1.0);

			
	}
	else
	{
		//fragColor = globalAmbient * material.ambient + light.ambient * material.ambient;
		fragColor = vec4((ambient + diffuse + specular), 1.0);

			fragColor += vec4((ambient + diffuse + specular), 1.0);
		//fragColor += light.diffuse * material.diffuse * max(dot(L,N),0.0)
			//	+ light.specular * material.specular
			//	* pow(max(dot(H,N),0.0),material.shininess*3.0);
		

		
	}

	
	
	//fragColor = texColor;
	//fragColor = fragColor * texColor;
	//fragColor = vec4((texColor * (globalAmbient + light.ambient + light.diffuse * max(dot(L,N),0.0)) + light.specular * pow(max(dot(H,N),0.0),	material.shininess*3.0);
}

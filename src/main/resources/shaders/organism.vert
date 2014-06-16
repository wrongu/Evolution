#version 150

uniform mat4 projection;

in vec2 vertex;   // mesh vertex
in vec2 position; // (instanced) position of organism (in place of model matrix)
in float energy;  // (instanced) energy of organism

out float f_energy;

void main(){
	// map from [0, inf) to [0,1)
	f_energy = 1 - exp(-energy);
	vec2 offset = vertex + position;
	gl_Position = projection * vec4(offset, 0.0, 1.0);
}
#version 150

uniform mat4 projection; // camera projection from world to screen
in vec2 vertex; // mesh vertex

uniform instanceBlock { // block of uniforms to be buffered for multiple organisms (instead of instancing)
	vec2 center;  // center x,y of organism
	float energy; // energy of organism
};

// energy mapped from [0,inf) to [0,1)
out float norm_energy;

void main(){
	norm_energy = 1 - exp(-energy);
	gl_Position = projection * vec4(vertex + center, 0.0, 1.0);
}
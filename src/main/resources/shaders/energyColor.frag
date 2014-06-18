#version 150

uniform float energy;
in vec2 world_coordinate;
out vec4 frag_color;

void main(){
	float energy_0_1 = 1 - exp(-energy);
	frag_color = vec4(1-energy_0_1, energy_0_1, 0.0, 1.0);
}
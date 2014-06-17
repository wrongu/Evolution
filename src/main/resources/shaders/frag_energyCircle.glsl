#version 150

in float norm_energy;

out vec4 frag_color;

void main(){
	// norm_energy = 0 --> red
	// norm_energy = 1 --> green
	frag_color = vec4(1-norm_energy, norm_energy, 0.0, 1.0);
}
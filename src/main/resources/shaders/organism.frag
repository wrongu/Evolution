#version 150

in float f_energy;
out vec4 frag_color;

void main(){
	frag_color = vec4(1-f_energy, f_energy, 0.0, 1.0);
}
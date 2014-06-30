#version 150

uniform vec3 rgb;
out vec4 glFragColor;

void main(){
	glFragColor = vec4(rgb, 1.0);
}
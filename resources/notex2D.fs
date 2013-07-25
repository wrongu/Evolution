#version 110

// FRAGMENT SHADER
// "default" implementation without texture

// inputs
varying vec4 Color;

void main(){
	gl_FragColor = Color;
}
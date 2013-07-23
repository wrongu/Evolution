#version 110

// FRAGMENT SHADER
// "default" implementation - computes color in terms of texture and color

// inputs
varying vec2 TexCoord;
varying vec4 Color;

// uniforms
uniform sampler2D Texture;

void main(){
	gl_FragColor = Color * texture2D(Texture, TexCoord);
}
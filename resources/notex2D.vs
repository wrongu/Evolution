#version 110

// VERTEX SHADER
// "default" implementation - position and color only

// uniforms: matrices for vertex transformation
uniform mat4 mModelView;
uniform mat4 mProjection;

// inputs
attribute vec2 position;
attribute vec4 color;

// outputs
varying vec4 Color;

void main(){
	Color = color;
	gl_Position = mModelView * mProjection * vec4(position, 0.0, 1.0);
}
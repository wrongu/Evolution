#version 110

// VERTEX SHADER
// "default" implementation - takes position2D, texture2D, colorRGB in. Transforms the vertex position according
// to the model/view/projection matrices, and passes color and texture to the fragment shader

// uniforms: matrices for vertex transformation
uniform mat4 mModelView;
uniform mat4 mProjection;

// inputs
attribute vec2 position;
attribute vec2 texUV;
attribute vec4 color;

// outputs
varying vec2 TexCoord;
varying vec4 Color;

void main(){
	TexCoord = texUV;
	Color = color;
	gl_Position = mModelView * mProjection * vec4(position, 0.0, 1.0);
}
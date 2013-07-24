#version 110

/// Fragment shader for performing a seperable blur on the specified texture. 

/// Uniform variables. 
uniform sampler2D Texture;
uniform sampler1D Gaussian;
uniform int Orientation;
uniform int BlurWidth;
uniform int TextureWidth;

/// Inputs from vertex Shader 
varying vec2 TexCoord;
varying vec4 Color;

const vec4 yellow = vec4(1.0, 1.0, 0.0, 1.0);

void main () {

	// initialize summed color
	vec4 texColor = vec4(0.0);
	vec4 color = vec4(0.0);
	
	float texwf = float(TextureWidth);
	float widthf = float(BlurWidth);
	float halfWidth = widthf / 2.0;
	
	float gauss_sum = 0.0;
	
	if ( Orientation == 0 ) {
		// Horizontal blur
		for (int i = 0; i < BlurWidth; ++i) {
			float x = (float(i) - 5.0) / 100.0;
			vec4 gauss = texture1D(Gaussian, float(i) / widthf);
			gauss_sum += gauss.x;
			texColor = texture2D(Texture, TexCoord + vec2(x, 0.0));
			color = color + gauss.x * texColor;
		}
	} else if(Orientation == 1){
		// Vertical blur
		for (int i = 0; i < BlurWidth; ++i) {
			float x = (float(i) - 5.0) / 100.0;
			vec4 gauss = texture1D(Gaussian, float(i) / widthf);
			gauss_sum += gauss.x;
			texColor = texture2D(Texture, TexCoord + vec2(0.0, x));
			color = color + gauss.x * texColor;
		}
	} else{
		color = yellow;
	}
	
	if(gauss_sum > 0.0) color = color / gauss_sum;
	
	// Apply color
	gl_FragColor = clamp(color, 0.0, 1.0);
	gl_FragColor.w = 1.0;
}
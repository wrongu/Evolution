
/// Fragment shader for performing a seperable blur on the specified texture. 
#ifdef GL_ES
	precision highp float;
#endif

/// Uniform variables. 
uniform vec2 TexelSize;
uniform sampler2D Sample0;
uniform int Orientation;
uniform int BlurAmount;
uniform float BlurScale;
uniform float BlurStrength;

/// Varying variables. 
varying vec2 vUv;

/// Gets the Gaussian value in the first dimension. 
/// Distance from origin on the x-axis.
/// Standard deviation.
/// The gaussian value on the x-axis.
float Gaussian (float x, float deviation) {
	return (1.0 / sqrt(2.0 * 3.141592 * deviation)) * exp(-((x * x) / (2.0 * deviation)));
} 

/// Fragment shader entry. 
void main () {
	// Locals
	float halfBlur = float(BlurAmount) * 0.5;
	vec4 colour = vec4(0.0);
	vec4 texColour = vec4(0.0);

	// Gaussian deviation
	float deviation = halfBlur * 0.35;
	deviation *= deviation;
	float strength = 1.0 - BlurStrength;

	if ( Orientation == 0 ) {
		// Horizontal blur
		for (int i = 0; i < 10; ++i) {
			if ( i >= BlurAmount ) break;
			float offset = float(i) - halfBlur;
			texColour = texture2D(Sample0, vUv + vec2(offset * TexelSize.x * BlurScale, 0.0)) * Gaussian(offset * strength, deviation);
			colour += texColour;
		}
	} else { 
		// Vertical blur
		for (int i = 0; i < 10; ++i) { 
			if ( i >= BlurAmount ) break;
			float offset = float(i) - halfBlur;
			texColour = texture2D(Sample0, vUv + vec2(0.0, offset * TexelSize.y * BlurScale)) * Gaussian(offset * strength, deviation);
			colour += texColour;
		}
	}
	
	// Apply colour
	gl_FragColor = clamp(colour, 0.0, 1.0);
	gl_FragColor.w = 1.0;
}
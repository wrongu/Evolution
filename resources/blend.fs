#version 110

/// Fragment shader for blending two textures using an algorithm that overlays the glowmap. 

/// Uniform variables. 
uniform sampler2D Underlay; // basic scene
uniform sampler2D Overlay;  // pre-blurred glow map 
uniform int BlendMode;

/// inputs from vertex shader 
varying vec2 TexCoord;
varying vec4 Color;

void main () {
	vec4 scene = texture2D(Underlay, TexCoord);
	vec4 glow = texture2D(Overlay, TexCoord);
	
	if( BlendMode == 0 ) {
		// Additive blending (strong result, high overexposure)
		gl_FragColor = clamp(glow + scene, 0.0, 1.0);
		gl_FragColor.w = 1.0;
	} else if( BlendMode == 1 ) {
		// Screen blending (mild result, medium overexposure)
		gl_FragColor = clamp((glow + scene) - (glow * scene), 0.0, 1.0);
		gl_FragColor.w = 1.0;
	} else if( BlendMode == 2 ) {
		// Softlight blending (light result, no overexposure)
		// Due to the nature of soft lighting, we need to bump the black region of the glowmap
		// to 0.5, otherwise the blended result will be dark (black soft lighting will darken 
		// the image).
		glow = (glow * 0.5) + 0.5;
		gl_FragColor.xyz = vec3(
		(glow.x <= 0.5) ? (scene.x - (1.0 - 2.0 * glow.x) * scene.x * (1.0 - scene.x)) : (((glow.x > 0.5) && (scene.x <= 0.25)) ? (scene.x + (2.0 * glow.x - 1.0) * (4.0 * scene.x * (4.0 * scene.x + 1.0) * (scene.x - 1.0) + 7.0 * scene.x)) : (scene.x + (2.0 * glow.x - 1.0) * (sqrt(scene.x) - scene.x))),
		(glow.y <= 0.5) ? (scene.y - (1.0 - 2.0 * glow.y) * scene.y * (1.0 - scene.y)) : (((glow.y > 0.5) && (scene.y <= 0.25)) ? (scene.y + (2.0 * glow.y - 1.0) * (4.0 * scene.y * (4.0 * scene.y + 1.0) * (scene.y - 1.0) + 7.0 * scene.y)) : (scene.y + (2.0 * glow.y - 1.0) * (sqrt(scene.y) - scene.y))),
		(glow.z <= 0.5) ? (scene.z - (1.0 - 2.0 * glow.z) * scene.z * (1.0 - scene.z)) : (((glow.z > 0.5) && (scene.z <= 0.25)) ? (scene.z + (2.0 * glow.z - 1.0) * (4.0 * scene.z * (4.0 * scene.z + 1.0) * (scene.z - 1.0) + 7.0 * scene.z)) : (scene.z + (2.0 * glow.z - 1.0) * (sqrt(scene.z) - scene.z))));
		gl_FragColor.w = 1.0;
	} else {
		// Show just the glow map
		gl_FragColor = glow;
	}
}
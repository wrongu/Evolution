#version 150

uniform int octaves;
uniform float t_size;
uniform float scale;
uniform sampler1D table;

//varying vec2 world_coordinate;
in vec2 world_coordinate;
out vec4 glFragColor;

float pseudo_rand2(float x, float y){
	// equivalent to the RNG algorithm from the java source (PerlinGenerator.java)
	x = mod(x, t_size) / t_size;
	y = mod(y, t_size) / t_size;
//	float w = texture1D(table, x).r + y;
//	return texture1D(table, w).r;
	float w = texture(table, x).r + y;
	return texture(table, w).r;
}

float lerp(float a, float b, float bias){
	float s = bias * bias * (3.0 - 2.0 * bias);
	return a + (b - a) * s;
}

void main(){
	float val = 0.0;
	// max_amp based on the series 1 + 1/2 + 1/4 + 1/8, 
	// which converges to 2 (but will be short of 2 since it's finite)
	float max_amp = 2.0 - pow(2.0, float(1-octaves));
	for(int o=0; o<octaves; o++){
		int pow = octaves-o-1;
		float factor = 1.0;
		while(pow-- > 0) factor *= 2.0;
		// version 120 doesn't allow bitwise operators, otherwise we would just do:
		//float factor = float(1 << (octaves - o - 1));
		float amp = 1.0 / factor;
		float width = scale * amp;
		float xlo = floor(world_coordinate.x / width);
		float ylo = floor(world_coordinate.y / width);
		// get the gradient directions
		float d00 = 6.283185307 * pseudo_rand2(xlo,ylo);
		float d01 = 6.283185307 * pseudo_rand2(xlo,ylo+1);
		float d10 = 6.283185307 * pseudo_rand2(xlo+1,ylo);
		float d11 = 6.283185307 * pseudo_rand2(xlo+1,ylo+1);
		// get offsets internal to cell for interpolation
		float xoff = world_coordinate.x / width - xlo;
		float yoff = world_coordinate.y / width - ylo;
		// dot product for effect of gradient on point
		float c00 = cos(d00) * xoff + sin(d00) * yoff;
		float c01 = cos(d01) * xoff + sin(d01) * (yoff-1.0);
		float c10 = cos(d10) * (xoff-1.0) + sin(d10) * yoff;
		float c11 = cos(d11) * (xoff-1.0) + sin(d11) * (yoff-1.0);
		// interpolate
		float bottom = lerp(c00, c10, xoff);
		float top = lerp(c01, c11, xoff);
		val += amp * lerp(bottom, top, yoff);
	}
	// map from [-1,1] to [0,1]
	val = (val + max_amp) / (2*max_amp);
	val = clamp(val, 0.0, 1.0);	
//	val = val > 0.25 ? 4/3*(val - 0.25) : 0;
//	val = val*val;
	glFragColor = vec4(val, val, val, 1.0);
}
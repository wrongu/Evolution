#version 130

uniform int octaves;
uniform int t_size;
uniform float scale;
uniform sampler1D table;

in vec2 world_coordinate;

int table_modulo(int i){
	int ret = (i % t_size);
	if(ret < 0) ret += t_size;
	return ret;
}

float pseudo_rand2(int x, int y){
	// copy of the RNG algorithm from the java source (PerlinGenerator.java)
	int i = table_modulo(x);
	i = table_modulo(int(texture(table, float(i) / float(t_size))) + y);
	return float(int(texture(table, float(i) / float(t_size)))) / float(t_size);
}

float lerp(float a, float b, float bbias){
	return bbias*bbias*(bbias*a + b);
}

void main(){
	float val = 0.0;
	// max_amp based on the series 1 + 1/2 + 1/4 + 1/8, 
	// which converges to 2 (but will be short of 2 since it's finite)
	float max_amp = 2.0 - pow(2.0, float(1-octaves));
	for(int o=0; o<octaves; o++){
		float factor = float(1 << (octaves - o - 1));
		float amp = 1.0 / factor;
		float width = scale * amp;
		int xlo = int(floor(world_coordinate.x/width));
		int ylo = int(floor(world_coordinate.y/width));
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
	val = (val + max_amp) / (2*max_amp);
	val = clamp(val, 0.0, 1.0);
	gl_FragColor = vec4(val, val, val, 1.0);
}
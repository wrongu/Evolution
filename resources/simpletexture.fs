

in vec2 TexCoord;
uniform Sampler tex;

void main(){
	gl_FragColor = texture(tex, TexCoord);
	gl_FragColor.w = 1.0;
}
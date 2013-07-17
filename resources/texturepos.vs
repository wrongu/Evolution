in vec2 screenCoord;
in vec2 texCoord
out vec2 TexCoord;

void main(){
	gl_Position = vec4(screenPos, 0.0, 1.0);
	TexCoord = texCoord;
}
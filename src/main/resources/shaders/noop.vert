#version 330
in vec3 vp;
uniform mat4 projection;

void main(){
	gl_Position = projection * vec4(vp, 1.0);
}
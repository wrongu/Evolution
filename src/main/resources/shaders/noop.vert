#version 330
in vec3 vp;
uniform mat4 projection;
out vec2 world_coordinate;

void main(){
	world_coordinate = vp.xy;
	gl_Position = projection * vec4(vp, 1.0);
}
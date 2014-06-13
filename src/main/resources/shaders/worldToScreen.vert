#version 150

uniform mat4 projection;
uniform mat4 model;

in vec2 vp;

out vec2 world_coordinate;

void main(){
	world_coordinate = vp.xy;
	gl_Position = projection * model * vec4(vp, 0.0, 1.0);
}
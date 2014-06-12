#version 330

uniform mat4 inverse_projection;

in vec2 screen_coordinate;

out vec2 world_coordinate;

void main(){
	world_coordinate = (inverse_projection * vec4(screen_coordinate, 0, 1)).xy;
	gl_Position = vec4(screen_coordinate, 0, 1);
}
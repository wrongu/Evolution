#version 150

uniform mat4 inverse_projection;

// attribute vec2 screen_coordinate;
in vec2 screen_coordinate;

//varying vec2 world_coordinate;
out vec2 world_coordinate;

void main(){
	world_coordinate = (inverse_projection * vec4(screen_coordinate, 0, 1)).xy;
	gl_Position = vec4(screen_coordinate, 0, 1);
}
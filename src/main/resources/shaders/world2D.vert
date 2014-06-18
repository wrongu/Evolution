#version 130

uniform float camera_x;
uniform float camera_y;
uniform float camera_zoom;
uniform float aspect;

out vec2 world_coordinate;

void main(){
	// SCREEN coordinates unchanged
	gl_Position = gl_Vertex;
	float wx = (gl_Vertex.x + camera_x) * camera_zoom;
	float wy = (gl_Vertex.y + camera_y) * camera_zoom;
	world_coordinate = vec2(wx, wy);
}
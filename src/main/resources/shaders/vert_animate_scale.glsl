#version 150

uniform mat4  projection;

uniform effectInstance{
	vec2  translate;
	float rotate;
	float effect;
};

in vec3  mesh;
out vec2 world_coordinate;

void main(){
	/* 'mesh' has 3 components: x and y are mesh coordinates (when the mesh is
		at the origin). mesh.z is the 'skeleton' component.	it encodes the 
		extent to which this vertex is affected by the 'effect' multiplier. if 
		mesh.z is zero, then 'effect' does nothing. */
	vec2 animated = mesh.xy * (1 + mesh.z * effect);
	/* apply model transformations */
	float s = sin(rotate);
	float c = cos(rotate);
	float rot_x = c*animated.x - s*animated.y;
	float rot_y = s*animated.x + c*animated.y;
	world_coordinate = vec2(rot_x, rot_y) + translate;
	/* project to screen space */
	gl_Position = projection * vec4(world_coordinate, 0.0, 1.0);
}
#version 150

uniform mat4 projection;

in vec3 vertex;     // mesh vertex
in vec2 position;   // (instanced) position of organism (in place of model matrix)
in float speed;     // (instanced) speed of organism
in float direction; // (instanced direction of motion)

void main(){
	// extend tail of kite by speed
	float speed_transform = vertex.z;
	vec2 kite_scaled = vertex.xy * (1 + speed_transform * speed);
	float c = cos(direction);
	float s = sin(direction);
	// apply rotation to direction of movement
	float rot_x = kite_scaled.x * c - kite_scaled.y * s;
	float rot_y = kite_scaled.x * s + kite_scaled.y * c;
	// move by position offset to final world location
	vec2 world = vec2(rot_x, rot_y) + position;
	// apply projection matrix, mapping world => screen
	gl_Position = projection * vec4(world, 0.0, 1.0);
}
#version 150

uniform mat4 projection;
in vec3 vertex; // mesh vertex

uniform instanceBlock {
	vec2 center;
	vec2 velocity;
};

void main(){
	float scale = vertex.z;
	float speed = sqrt(velocity.x*velocity.x + velocity.y*velocity.y);
	vec2 speed_scaled = vertex.xy * (1 + speed * scale);
	// default: no rotation
	float c = 1.0;
	float s = 0.0;
	if(speed > 0.0001){
		c = velocity.x / speed;
		s = velocity.y / speed;
	}
	
	float rot_x = speed_scaled.x * c - speed_scaled.y * s;
	float rot_y = speed_scaled.y * c + speed_scaled.x * s;
	
	vec2 offset = center  + vec2(rot_x, rot_y);
	
	gl_Position = projection * vec4(offset, 0.0, 1.0);
}
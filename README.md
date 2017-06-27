# OpenGL-Scene
The dependencies needed to run this are
-JOGL and GraphicsLib3D
-If need be, you can compile the scene with compile.bat and then run the program with run.bat

![Alt text](/images/Scene.jpg?raw=true "")

## Controls

###### Controls for camera.
A – camera Left	

J – rotate camera right

D – camera Right			

L – rotate camera left

W – camera forward			

K – rotate camera down

S – camera backward			

I – rotate camera up

Controls for light source.
Mouse wheel up – move light source forward
Mouse wheel down – move light source backward

###### Controls for light source.
Mouse wheel up – move light source forward
Mouse wheel down – move light source backward
Mouse drag – drag the mouse with the left mouse button clicked to drag the light source along the X and Y plane.
B button – remove light source model. (NOTE) – does not stop the light source from being rendered onto objects.

## How I created some of the objects and textures.

- Shadow mapping is done on three objects. It is done by having two shadows cast from the half torus or tire and the stack of wood  onto the long piece of wood(scaled cube).
- I procedurally did bump mapping on the torus to give it a rugged look and attempted to make it look more like a tire sticking out of wood.
- I used a tessellation shader in conjunction with a height map to create a rugged swampy/dark terrain. Also, I did it with instancing.
- I used a user defined clipping plane to cut my torus in half. To give it the appearance of a tire sticking out of the wood. (NOTE) – you can zoom in and see that the fragment clipping plane cuts off half the torus.
- I used the skybox for environment mapping to create the very bad illusion of windows reflecting from eye space. The texture changes as the camera moves.
- I used Perlin noise to apply a wood material to the wood stack object and to the scaled cube object to give it a more realistic wood look.

## CREDITS AND ATTRIBUTION FOR MATERIALS/OBJECTS/IMAGES USED IN SCENE.

Dark_Swamp.jpg – Author/Owner  http://spiralgraphics.biz/packs/usage_rights.htm 



package a4;

/*
 * Adriane Rambaran
 * Assignment 4 CSC155.
 */
import graphicslib3D.*;
import graphicslib3D.light.*;
import graphicslib3D.GLSLUtils.*;
import graphicslib3D.shape.*;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.io.IOException;
import java.nio.*;
import java.util.Random;

import javax.swing.*;

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_CCW;
import static com.jogamp.opengl.GL.GL_CLAMP_TO_EDGE;
import static com.jogamp.opengl.GL.GL_CULL_FACE;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TEXTURE2;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_TEXTURE_CUBE_MAP;
import static com.jogamp.opengl.GL.GL_TEXTURE_WRAP_S;
import static com.jogamp.opengl.GL.GL_TEXTURE_WRAP_T;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.*;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;

import com.jogamp.common.nio.Buffers;
import com.jogamp.newt.event.KeyEvent;

public class Code extends JFrame implements GLEventListener, MouseMotionListener, MouseWheelListener, MouseListener
{	
	private GLCanvas myCanvas;
	private Material thisMaterial;
	private String[] vBlinn1ShaderSource, vBlinn2ShaderSource, fBlinn2ShaderSource, vertexShaderSource, fragmentShaderSource, cubeVertShaderSource, cubeFragShaderSource, tessCShaderSource, tessEShaderSource, vertexTShaderSource, fragmentTShaderSource;
	private int rendering_program1, rendering_program2,rendering_program3, rendering_program_cube_map, rendering_program4;
	private int vao[] = new int[1];
	private int vbo[] = new int[18];
	private int mv_location, proj_location, vertexLoc, n_location, t_location;
	private float aspect;
	private GLSLUtils util = new GLSLUtils();
	
	// location of torus and camera
	private Point3D torusLoc = new Point3D(5, 1.0, -0.3);
	private Point3D pyrLoc = new Point3D(5.0, .45, -6.3);
	private Point3D houseLoc = new Point3D(-2.0, .1, -4.3);
	private Point3D woodStackLoc = new Point3D(5, 1.0, -3);
	private Point3D cameraLoc = new Point3D(0, 4.1, 25.0);
	private Point3D lightLoc = new Point3D(-1.8f, 2.2f, 20f);
	private Point3D stationaryLightLoc = new Point3D(-5, 4, 1.1f);
	private Point3D pointLookAt = new Point3D(8, .5f, 2.5f);
	
	private Matrix3D m_matrix = new Matrix3D();
	private Matrix3D v_matrix = new Matrix3D();
	private Matrix3D mv_matrix = new Matrix3D();
	private Matrix3D proj_matrix = new Matrix3D();
	private Matrix3D mvp_matrix = new Matrix3D();
	//Camera Control stuff
	private Vector3D upAxis = new Vector3D();
	private Vector3D rightAxis = new Vector3D();
	private double degrees = 0;
	private double degrees2 = 0;
	
	// light stuff
	private float [] globalAmbient = new float[] { 0.7f, 0.7f, 0.7f, 1.0f };
	private PositionalLight currentLight = new PositionalLight();
	private PositionalLight stationaryLight = new PositionalLight();

	
	// shadow stuff
	private int scSizeX, scSizeY;
	private int [] shadow_tex = new int[1];
	private int [] shadow_buffer = new int[1];
	private Matrix3D lightV_matrix = new Matrix3D();
	private Matrix3D lightP_matrix = new Matrix3D();
	private Matrix3D shadowMVP1 = new Matrix3D();
	private Matrix3D shadowMVP2 = new Matrix3D();
	private Matrix3D b = new Matrix3D();

	// model stuff

	private ImportedModel house = new ImportedModel("House.obj");
	private ImportedModel cube = new ImportedModel("cube.obj");
	private ImportedModel woodStack = new ImportedModel("StackOfWood.obj");

	private Material materialObsidian, materialWood;
	
	private int cubeTexture, sphereTexture, houseTexture, woodStackTexture;
	private Texture joglHouseTexture, joglSphereTexture, joglCubeTexture, joglWoodStackTexture, joglGroundTexture;
	private boolean showAxes = true;
	private float[] objectColor = new float[4];
	private Torus myTorus = new Torus(0.6f, 0.4f, 48);
	private int numPyramidVertices, numTorusVertices, numWoodStackVertices, numStreetLampVertices;
	private int numCubeVertices, numHouseVertices;
	private float use_tex = 1;
	private float use_bump = 1;
	private float reflect_tex =1;
	private float use_noise =1;
	private float use_tess = 1;
	private int flipNormal =0;
	private Matrix3D texRot = new Matrix3D();
	private int textureID;
	private int noiseHeight= 300;
	private int noiseWidth = 300;
	private int noiseDepth = 300;
	private double[][][] noise = new double[noiseHeight][noiseWidth][noiseDepth];
	private Random random = new Random();
	
	
	//Control Stuff
	private int prevMouseMovedX;
	private int prevMouseMovedY;
	private int currMouseMovedX;
	private int currMouseMovedY;
	private boolean lightActive = true;
	
	//SkyBox Stuff
	private Texture tex;
	private int textureID1, textureID0, textureID3, textureID2;
	private Matrix3D cubeV_matrix = new Matrix3D();
	private float amt = 0.0f;
	

	public Code()
	{	setTitle("SceneFour");
		setSize(800, 800);
		myCanvas = new GLCanvas();
		myCanvas.addGLEventListener(this);
		myCanvas.addMouseWheelListener(this);
		myCanvas.addMouseListener(this);
		myCanvas.addMouseMotionListener(this);
		getContentPane().add(myCanvas);
		setVisible(true);
		this.setupKeys();
		FPSAnimator animator = new FPSAnimator(myCanvas, 30);
		animator.start();
	}

	public void renderRestOfScene()
	{
		GL4 gl = (GL4) GLContext.getCurrentGL();
		gl.glUseProgram(rendering_program3);
		gl.glEnable(GL_DEPTH_TEST);
		int mv_location = gl.glGetUniformLocation(rendering_program3, "mv_matrix");
		int proj_location = gl.glGetUniformLocation(rendering_program3, "proj_matrix");
		int n_location = gl.glGetUniformLocation(rendering_program3, "norm_matrix");
		int color = gl.glGetUniformLocation(rendering_program3, "color");
		int tex_use = gl.glGetUniformLocation(rendering_program3, "use_tex");
		int tess_use = gl.glGetUniformLocation(rendering_program3, "use_tess");
		float aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
		proj_matrix = perspective(50.0f, aspect, 0.1f, 1000.0f);
		
		//m_matrix.setToIdentity();
		//v_matrix.setToIdentity();
		installLights(rendering_program3,currentLight, v_matrix);
		/*if(showAxes)
		{
			objectColor = new float[]{1.0f, 0.0f, 0.0f, 1.0f};
			gl.glProgramUniform4fv(rendering_program3, color, 1, objectColor, 0);
			m_matrix.setToIdentity();
			//m_matrix.translate(lightLoc.getX(), lightLoc.getY(), lightLoc.getZ());
			//m_matrix.rotateX(35.0f);
			m_matrix.scale(10f, 10f, 10f);
			
			mv_matrix.setToIdentity();
			mv_matrix.concatenate(v_matrix);
			mv_matrix.concatenate(m_matrix);
			
			gl.glUniformMatrix4fv(mv_location, 1, false, mv_matrix.getFloatValues(), 0);
			gl.glUniformMatrix4fv(proj_location, 1, false, proj_matrix.getFloatValues(), 0);
			gl.glUniformMatrix4fv(n_location, 1, false, (mv_matrix.inverse()).transpose().getFloatValues(),0);
			//Axis
			gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[7]);
			gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(0);
			gl.glDrawArrays(GL_LINE_LOOP, 0, 12); 
			gl.glEnable(GL_DEPTH_TEST);
			objectColor = new float[]{1.0f, 0.0f, 0.0f, 0.0f};
			gl.glProgramUniform4fv(rendering_program3, color, 1, objectColor, 0);
			System.out.println("hell");
		}*/
		
		use_tex = 1;
		use_tess = 0;
		m_matrix.setToIdentity();
		m_matrix.translate(houseLoc.getX(), houseLoc.getY(), houseLoc.getZ());
		m_matrix.scale(0.5, 0.5, 0.5);
		m_matrix.rotate(180, rightAxis);
		
		mv_matrix.setToIdentity();
		mv_matrix.concatenate(v_matrix);
		mv_matrix.concatenate(m_matrix);
		
		gl.glUniformMatrix4fv(mv_location, 1, false, mv_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(proj_location, 1, false, proj_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(n_location, 1, false, (mv_matrix.inverse()).transpose().getFloatValues(),0);
		gl.glProgramUniform1f(rendering_program3, tex_use, use_tex);
		gl.glProgramUniform1f(rendering_program3, tess_use, use_tess);
		//Vertex
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[4]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		//Normals
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[5]);
		gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

		//Texture
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[6]);
		gl.glVertexAttribPointer(2, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);
		
		gl.glActiveTexture(GL_TEXTURE1);
		gl.glBindTexture(GL_TEXTURE_2D, houseTexture);
		
		
		//gl.glClear(GL_DEPTH_BUFFER_BIT);
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);

		gl.glDrawArrays(GL_TRIANGLES, 0, numHouseVertices);
		
		//Tessalator section.
		use_tess = 1;
		use_tex = 1;
		gl.glUniformMatrix4fv(mv_location, 1, false, mv_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(proj_location, 1, false, proj_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(n_location, 1, false, (mv_matrix.inverse()).transpose().getFloatValues(),0);
		gl.glProgramUniform1f(rendering_program3, tex_use, use_tex);
		gl.glProgramUniform1f(rendering_program3, tess_use, use_tess);
		
		objectColor = new float[]{1.0f, 1.0f, 0.0f, 1.0f};
		if(lightActive)
		{
			m_matrix.setToIdentity();
			m_matrix.translate(lightLoc.getX(), lightLoc.getY(), lightLoc.getZ());
			//m_matrix.rotateX(35.0f);
			m_matrix.scale(.3f, .1f, .3f);
			
			mv_matrix.setToIdentity();
			mv_matrix.concatenate(v_matrix);
			mv_matrix.concatenate(m_matrix);
			
			gl.glUniformMatrix4fv(mv_location, 1, false, mv_matrix.getFloatValues(), 0);
			gl.glUniformMatrix4fv(proj_location, 1, false, proj_matrix.getFloatValues(), 0);
			gl.glUniformMatrix4fv(n_location, 1, false, (mv_matrix.inverse()).transpose().getFloatValues(),0);
			
			//Set color for light object
			gl.glProgramUniform4fv(rendering_program3, color, 1, objectColor, 0);
			
			//Vertex
			gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[7]);
			gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(0);
			
			//Normals
			gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[8]);
			gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(1);
			
			
			gl.glEnable(GL_CULL_FACE);
			gl.glFrontFace(GL_CCW);
			gl.glEnable(GL_DEPTH_TEST);
			gl.glDepthFunc(GL_LEQUAL);
	
			gl.glDrawArrays(GL_TRIANGLES, 0, numCubeVertices);
			objectColor = new float[]{1.0f, 1.0f, 0.0f, 0.0f};
			gl.glProgramUniform4fv(rendering_program3, color, 1, objectColor, 0);
		}
		
		gl.glUseProgram(rendering_program4);
		int mvp_location = gl.glGetUniformLocation(rendering_program4, "mvp");
		
		m_matrix.setToIdentity();
		m_matrix.translate(0, 0, -5);
		m_matrix.scale(20, 30, 40);
		m_matrix.rotateY(90);
		mv_matrix.setToIdentity();
		mv_matrix.concatenate(v_matrix);
		mv_matrix.concatenate(m_matrix);
		
		mvp_matrix.setToIdentity();
		mvp_matrix.concatenate(proj_matrix);
		mvp_matrix.concatenate(v_matrix);
		mvp_matrix.concatenate(m_matrix);
		
		gl.glUniformMatrix4fv(mvp_location, 1, false, mvp_matrix.getFloatValues(),0);
		
		gl.glActiveTexture(GL_TEXTURE1);
		gl.glBindTexture(GL_TEXTURE_2D, textureID0);
		gl.glActiveTexture(GL_TEXTURE4);
		gl.glBindTexture(GL_TEXTURE_2D, textureID1);
		
		gl.glEnable(GL_DEPTH_TEST);
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CW);
		
		gl.glPatchParameteri(GL_PATCH_VERTICES, 4);
		gl.glDrawArraysInstanced(GL_PATCHES, 0, 4, 64*64);
	}
	public void display(GLAutoDrawable drawable)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		
		
		currentLight.setPosition(lightLoc);
		aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();	
		proj_matrix = perspective(50.0f, aspect, 0.1f, 1000.0f);
		
		float bkg[] = { 0.0f, 0.0f, 0.0f, 1.0f };
		FloatBuffer bkgBuffer = Buffers.newDirectFloatBuffer(bkg);
		gl.glClearBufferfv(GL_COLOR, 0, bkgBuffer);
		//  build the VIEW matrix

	
		// draw cube map
		
		
		
		

		gl.glBindFramebuffer(GL_FRAMEBUFFER, shadow_buffer[0]);
		gl.glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, shadow_tex[0], 0);
	
		gl.glDrawBuffer(GL_NONE);
		gl.glEnable(GL_DEPTH_TEST);

		gl.glEnable(GL_POLYGON_OFFSET_FILL);	// for reducing
		
		gl.glPolygonOffset(2.0f, 4.0f);			//  shadow artifacts

		passOne();
		
		gl.glDisable(GL_POLYGON_OFFSET_FILL);	// artifact reduction, continued
		
		gl.glBindFramebuffer(GL_FRAMEBUFFER, 0);
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, shadow_tex[0]);
	
		gl.glDrawBuffer(GL_FRONT);
		
		passTwo();
		
		
		renderRestOfScene();
	}
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	public void passOne()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		
		

		gl.glUseProgram(rendering_program1);
		
		Point3D origin = new Point3D(0.0, 0.0, 0.0);
		Vector3D up = new Vector3D(0.0, 1.0, 0.0);
		lightV_matrix.setToIdentity();
		lightP_matrix.setToIdentity();
	
		lightV_matrix = lookAt(currentLight.getPosition(), pointLookAt, up);	// vector from light to origin
		lightP_matrix = perspective(50.0f, aspect, 0.1f, 1000.0f);

		// draw the torus
		
		m_matrix.setToIdentity();
		m_matrix.translate(torusLoc.getX(),torusLoc.getY(),torusLoc.getZ());
		m_matrix.rotateZ(90.0);
		
		shadowMVP1.setToIdentity();
		shadowMVP1.concatenate(lightP_matrix);
		shadowMVP1.concatenate(lightV_matrix);
		shadowMVP1.concatenate(m_matrix);
		int shadow_location = gl.glGetUniformLocation(rendering_program1, "shadowMVP");
		gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP1.getFloatValues(), 0);
		
		// set up torus vertices buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);	
	
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);

		gl.glDrawArrays(GL_TRIANGLES, 0, numTorusVertices);

		// ---- draw the long cube.
		
		//  build the MODEL matrix
		m_matrix.setToIdentity();
		m_matrix.translate(pyrLoc.getX(),pyrLoc.getY(),pyrLoc.getZ());
		m_matrix.rotateZ(90.0);
		//m_matrix.rotateY(40.0);
		m_matrix.scale(0.3f, 2.6f, 3.8f);
		shadowMVP1.setToIdentity();
		shadowMVP1.concatenate(lightP_matrix);
		shadowMVP1.concatenate(lightV_matrix);
		shadowMVP1.concatenate(m_matrix);

		gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP1.getFloatValues(), 0);
		
		// set up vertices buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[7]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
	
		gl.glDrawArrays(GL_TRIANGLES, 0, numCubeVertices);
		
		// ----- Draw the Wood
		
		// Build the Model Matrix
		m_matrix.setToIdentity();
		m_matrix.translate(woodStackLoc.getX(), woodStackLoc.getY(), woodStackLoc.getZ());
		m_matrix.scale(0.3f, .3f, .3f);
		
		//m_matrix.scale(0.3f, 2.6f, 3.8f);
		shadowMVP1.setToIdentity();
		shadowMVP1.concatenate(lightP_matrix);
		shadowMVP1.concatenate(lightV_matrix);
		shadowMVP1.concatenate(m_matrix);

		gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP1.getFloatValues(), 0);
		
		//set up vertices buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[11]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
	
		gl.glDrawArrays(GL_TRIANGLES, 0, numWoodStackVertices);
		

		
	}
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	public void passTwo()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
	int viewLocation = gl.glGetUniformLocation(rendering_program2, "v_matrix");
	int modelLocation = gl.glGetUniformLocation(rendering_program2, "m_matrix");
	//DRAW CUBE MAP	
	float depthClearVal[] = new float[1]; depthClearVal[0] = 1.0f;
	gl.glClearBufferfv(GL_DEPTH,0,depthClearVal,0);
	
		v_matrix.setToIdentity();
		//v_matrix.setToIdentity();
		upAxis.setX(v_matrix.elementAt(0, 0));
		upAxis.setY(v_matrix.elementAt(0, 1));
		upAxis.setZ(v_matrix.elementAt(0, 2));
		rightAxis.setX(v_matrix.elementAt(1, 3));
		rightAxis.setY(v_matrix.elementAt(1, 1));
		rightAxis.setZ(v_matrix.elementAt(1, 2));
		v_matrix.rotate(degrees, upAxis);
		v_matrix.rotate(degrees2, rightAxis);
		v_matrix.translate(-cameraLoc.getX(),-cameraLoc.getY(),-cameraLoc.getZ());
				
		gl.glUseProgram(rendering_program_cube_map);

		//  put the V matrix into the corresponding uniforms
		cubeV_matrix = (Matrix3D) v_matrix.clone();
		cubeV_matrix.scale(1.0, -1.0, -1.0);
		int v_location = gl.glGetUniformLocation(rendering_program_cube_map, "v_matrix");
		gl.glUniformMatrix4fv(v_location, 1, false, cubeV_matrix.getFloatValues(), 0);
		
		// put the P matrix into the corresponding uniform
		int ploc = gl.glGetUniformLocation(rendering_program_cube_map, "p_matrix");
		gl.glUniformMatrix4fv(ploc, 1, false, proj_matrix.getFloatValues(), 0);
		
		// set up vertices buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[14]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glActiveTexture(GL_TEXTURE2);
		gl.glBindTexture(GL_TEXTURE_CUBE_MAP, textureID2);

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glDisable(GL_DEPTH_TEST);
		gl.glDrawArrays(GL_TRIANGLES, 0, 36);
		gl.glEnable(GL_DEPTH_TEST);
		
		
		//DRAW EVERYTHING ELSE
		
		gl.glUseProgram(rendering_program2);
		thisMaterial = materialObsidian;		
		mv_location = gl.glGetUniformLocation(rendering_program2, "mv_matrix");
		proj_location = gl.glGetUniformLocation(rendering_program2, "proj_matrix");
		n_location = gl.glGetUniformLocation(rendering_program2, "normalMat");
		t_location = gl.glGetUniformLocation(rendering_program2, "texRot");
		
		int tex_use = gl.glGetUniformLocation(rendering_program2, "use_tex");
		int shadow_location = gl.glGetUniformLocation(rendering_program2,  "shadowMVP");
		int bump_use = gl.glGetUniformLocation(rendering_program2, "use_bump");
		int reflect_use = gl.glGetUniformLocation(rendering_program2, "reflect_tex");
		int noise_use  = gl.glGetUniformLocation(rendering_program2, "noise_use");
		int flip_location = gl.glGetUniformLocation(rendering_program2, "flipNormal");
		
		
		installLights(rendering_program2,currentLight, v_matrix);
		
		gl.glUniformMatrix4fv(viewLocation, 1, false,v_matrix.getFloatValues(),0);
		
		m_matrix.setToIdentity();
		m_matrix.translate(torusLoc.getX(),torusLoc.getY(),torusLoc.getZ());
		m_matrix.rotateZ(90.0);
		
		//  build the MODEL-VIEW matrix
		mv_matrix.setToIdentity();
		mv_matrix.concatenate(v_matrix);
		mv_matrix.concatenate(m_matrix);
		
		shadowMVP2.setToIdentity();
		shadowMVP2.concatenate(b);
		shadowMVP2.concatenate(lightP_matrix);
		shadowMVP2.concatenate(lightV_matrix);
		shadowMVP2.concatenate(m_matrix);
		
		//  put the MV and PROJ matrices into the corresponding uniforms
		use_tex = 1;
		use_bump = 1;
		reflect_tex = 0;
		use_noise = 0;
		gl.glUniformMatrix4fv(mv_location, 1, false, mv_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(proj_location, 1, false, proj_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(n_location, 1, false, (mv_matrix.inverse()).transpose().getFloatValues(), 0);
		gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP2.getFloatValues(), 0);
		gl.glProgramUniform1f(rendering_program2, tex_use, use_tex);
		gl.glProgramUniform1f(rendering_program2, bump_use, use_bump);
		gl.glProgramUniform1f(rendering_program2, reflect_use, reflect_tex);
		
		// set up torus vertices buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		// set up torus normals buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
		gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);	
		
		//gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[9]);
		//gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
		//gl.glEnableVertexAttribArray(2);	
	
		//gl.glActiveTexture(GL_TEXTURE1);
		//gl.glBindTexture(GL_TEXTURE_2D, cubeTexture);
		
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
	
		gl.glEnable(GL_CLIP_DISTANCE0);
		
		gl.glUniform1i(flip_location, 0);
		gl.glFrontFace(GL_CCW);
		gl.glDrawArrays(GL_TRIANGLES, 0, numTorusVertices);
		
		gl.glUniform1i(flip_location, 1);
		gl.glFrontFace(GL_CW);
		gl.glDrawArrays(GL_TRIANGLES,0, numTorusVertices);
		
		gl.glUniform1i(flip_location, 0);
		gl.glDisable(GL_CLIP_DISTANCE0);
		//gl.glDrawArrays(GL_TRIANGLES, 0, numTorusVertices);

		// draw the WOOD SLAB, or just a scaled cube....
		
		thisMaterial = materialWood;		
		installLights(rendering_program2,currentLight, v_matrix);
		
		//  build the MODEL matrix
		m_matrix.setToIdentity();
		m_matrix.translate(pyrLoc.getX(),pyrLoc.getY(),pyrLoc.getZ());
		m_matrix.rotateZ(90.0);
		//m_matrix.rotateY(40.0);
		m_matrix.scale(0.32f, 2.2f, 11.7f);
		//  build the MODEL-VIEW matrix
		mv_matrix.setToIdentity();
		mv_matrix.concatenate(v_matrix);
		mv_matrix.concatenate(m_matrix);
		
		shadowMVP2.setToIdentity();
		shadowMVP2.concatenate(b);
		shadowMVP2.concatenate(lightP_matrix);
		shadowMVP2.concatenate(lightV_matrix);
		shadowMVP2.concatenate(m_matrix);
		gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP2.getFloatValues(), 0);

		//  put the MV and PROJ matrices into the corresponding uniforms
		use_tex = 0;
		use_bump = 0;
		use_noise =1;
		gl.glUniformMatrix4fv(mv_location, 1, false, mv_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(proj_location, 1, false, proj_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(n_location, 1, false, (mv_matrix.inverse()).transpose().getFloatValues(), 0);
		gl.glProgramUniform1f(rendering_program2, tex_use, use_tex);
		gl.glProgramUniform1f(rendering_program2, bump_use, use_bump);
		gl.glProgramUniform1f(rendering_program2, noise_use, use_noise);
		// set up vertices buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[7]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		// set up normals buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[8]);
		gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);

		gl.glDrawArrays(GL_TRIANGLES, 0, numCubeVertices);
		
		//-- Draw the Wood
		thisMaterial = materialWood;
		installLights(rendering_program2,currentLight, v_matrix);
		
		//  build the MODEL matrix
		m_matrix.setToIdentity();
		m_matrix.translate(woodStackLoc.getX(),woodStackLoc.getY(),woodStackLoc.getZ());
		m_matrix.scale(0.3f, .3f, .3f);
		
		//  build the MODEL-VIEW matrix
		mv_matrix.setToIdentity();
		mv_matrix.concatenate(v_matrix);
		mv_matrix.concatenate(m_matrix);
		
		shadowMVP2.setToIdentity();
		shadowMVP2.concatenate(b);
		shadowMVP2.concatenate(lightP_matrix);
		shadowMVP2.concatenate(lightV_matrix);
		shadowMVP2.concatenate(m_matrix);
		gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP2.getFloatValues(), 0);

		//  put the MV and PROJ matrices into the corresponding uniforms
		use_tex = 0;
		use_noise = 1;
		gl.glUniformMatrix4fv(mv_location, 1, false, mv_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(proj_location, 1, false, proj_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(n_location, 1, false, (mv_matrix.inverse()).transpose().getFloatValues(), 0);
		gl.glProgramUniform1f(rendering_program2, tex_use, use_tex);
		gl.glUniformMatrix4fv(t_location, 1, false, texRot.getFloatValues(), 0);
		gl.glProgramUniform1f(rendering_program2, noise_use, use_noise);
		// set up vertices buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[11]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		// set up normals buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[12]);
		gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

		gl.glActiveTexture(GL_TEXTURE3);
		gl.glBindTexture(GL_TEXTURE_3D, textureID);
		
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);

		gl.glDrawArrays(GL_TRIANGLES, 0, numWoodStackVertices);
		
		//Reflective Windows for House........................
		//Extremely detailed window One
		m_matrix.setToIdentity();
		m_matrix.translate(-4.5, 3.5, 0);
		m_matrix.scale(1,0.9, 0.1);
		gl.glUniformMatrix4fv(modelLocation, 1, false, m_matrix.getFloatValues(),0);
		mv_matrix.setToIdentity();
		mv_matrix.concatenate(v_matrix);
		mv_matrix.concatenate(m_matrix);
		reflect_tex =1;
		use_noise = 0;
		gl.glUniformMatrix4fv(mv_location, 1, false, mv_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(proj_location, 1, false, proj_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(n_location, 1, false, (mv_matrix.inverse()).transpose().getFloatValues(), 0);
		gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP2.getFloatValues(), 0);
		gl.glProgramUniform1f(rendering_program2, noise_use, use_noise);
		gl.glProgramUniform1f(rendering_program2, tex_use, use_tex);
		gl.glProgramUniform1f(rendering_program2, bump_use, use_bump);
		gl.glProgramUniform1f(rendering_program2, reflect_use, reflect_tex);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[7]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		// set up torus normal buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[8]);
		gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
		
		gl.glActiveTexture(GL_TEXTURE2);
		gl.glBindTexture(GL_TEXTURE_CUBE_MAP,textureID2);
		
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glDepthFunc(GL_LEQUAL);
	
		gl.glDrawArrays(GL_TRIANGLES, 0, numCubeVertices);
		
		
		//Extremely detailed window two.
		m_matrix.setToIdentity();
		m_matrix.translate(.6, 3.5, 0);
		m_matrix.scale(1,0.9, 0.1);
		gl.glUniformMatrix4fv(modelLocation, 1, false, m_matrix.getFloatValues(),0);
		mv_matrix.setToIdentity();
		mv_matrix.concatenate(v_matrix);
		mv_matrix.concatenate(m_matrix);
		reflect_tex =1;
		
		gl.glUniformMatrix4fv(mv_location, 1, false, mv_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(proj_location, 1, false, proj_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(n_location, 1, false, (mv_matrix.inverse()).transpose().getFloatValues(), 0);
		gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP2.getFloatValues(), 0);
		
		gl.glProgramUniform1f(rendering_program2, tex_use, use_tex);
		gl.glProgramUniform1f(rendering_program2, bump_use, use_bump);
		gl.glProgramUniform1f(rendering_program2, reflect_use, reflect_tex);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[7]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		// set up torus normal buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[8]);
		gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
		
		gl.glActiveTexture(GL_TEXTURE2);
		gl.glBindTexture(GL_TEXTURE_CUBE_MAP,textureID2);
		
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glDepthFunc(GL_LEQUAL);
	
		gl.glDrawArrays(GL_TRIANGLES, 0, numCubeVertices);
			
	}

	public void init(GLAutoDrawable drawable)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		createShaderPrograms();
		setupVertices();
		generateNoise();
		textureID = loadNoiseTexture();
		setupShadowBuffers();
		defineMaterials();
		b.setElementAt(0,0,0.5);b.setElementAt(0,1,0.0);b.setElementAt(0,2,0.0);b.setElementAt(0,3,0.5f);
		b.setElementAt(1,0,0.0);b.setElementAt(1,1,0.5);b.setElementAt(1,2,0.0);b.setElementAt(1,3,0.5f);
		b.setElementAt(2,0,0.0);b.setElementAt(2,1,0.0);b.setElementAt(2,2,0.5);b.setElementAt(2,3,0.5f);
		b.setElementAt(3,0,0.0);b.setElementAt(3,1,0.0);b.setElementAt(3,2,0.0);b.setElementAt(3,3,1.0f);
		
		joglHouseTexture = loadTexture("images/Farmhouse Texture.jpg");
		houseTexture = joglHouseTexture.getTextureObject();
		

		
		joglGroundTexture = loadTexture("images/The_Dark_Marshes.jpg");
		textureID0 = joglGroundTexture.getTextureObject();
		
		//mip mapping and antisotorpic filtering
		gl.glBindTexture(GL_TEXTURE_2D,textureID0);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
		gl.glGenerateMipmap(GL_TEXTURE_2D);
		if (gl.isExtensionAvailable("GL_EXT_texture_filter_anisotropic"))
		{	float aniso[] = new float[1];
			gl.glGetFloatv(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, aniso, 0);
			gl.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, aniso[0]);
		}
		
		joglGroundTexture = loadTexture("images/The_Dark_SwampBMAP.jpg");
		textureID1 = joglGroundTexture.getTextureObject();
		
		// apply mipmapping and anisotropic filtering
		gl.glBindTexture(GL_TEXTURE_2D, textureID1);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
		gl.glGenerateMipmap(GL_TEXTURE_2D);
		if (gl.isExtensionAvailable("GL_EXT_texture_filter_anisotropic"))
		{	float aniso[] = new float[1];
			gl.glGetFloatv(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, aniso, 0);
			gl.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, aniso[0]);
		}

		texRot.setToIdentity();
		texRot.rotateZ(35.0f);
		texRot.rotateY(90.0f);
		texRot.rotateZ(65.0f);
		
		textureID2 = loadCubeMap();
		
		gl.glEnable(GL_TEXTURE_CUBE_MAP_SEAMLESS);
		// may reduce shadow border artifacts
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
	}
	
	private void fillDataArray(byte data[])
	{ double xyPeriod = 20.0;
	  double turbPower = 0.1;
	  double turbSize = 16.0;
	  Color c;
	  
	  for (int i=0; i<noiseHeight; i++)
	  { for (int j=0; j<noiseWidth; j++)
	    { for (int k=0; k<noiseDepth; k++)
	      { 			
			double xValue = (i - (double)noiseHeight/2.0) / (double)noiseHeight;
			double yValue = (j - (double)noiseWidth/2.0) / (double)noiseWidth;
			double distValue = Math.sqrt(xValue * xValue + yValue * yValue)
								+ turbPower * turbulence(i, j, k, turbSize) / 200.0;
			double sineValue = 32.0 * Math.abs(Math.sin(2.0 * xyPeriod * distValue * 3.14159));
			
			c = new Color((int)(150+(int)sineValue), (int)(60+(int)sineValue), 0);
			
	        data[i*(noiseWidth*noiseHeight*4)+j*(noiseHeight*4)+k*4+0] = (byte) c.getRed();
	        data[i*(noiseWidth*noiseHeight*4)+j*(noiseHeight*4)+k*4+1] = (byte) c.getGreen();
	        data[i*(noiseWidth*noiseHeight*4)+j*(noiseHeight*4)+k*4+2] = (byte) c.getBlue();
	        data[i*(noiseWidth*noiseHeight*4)+j*(noiseHeight*4)+k*4+3] = (byte) 255;
	} } } }

	private int loadNoiseTexture()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();

		byte[] data = new byte[noiseHeight*noiseWidth*noiseDepth*4];
		
		fillDataArray(data);

		ByteBuffer bb = Buffers.newDirectByteBuffer(data);

		int[] textureIDs = new int[1];
		gl.glGenTextures(1, textureIDs, 0);
		int textureID = textureIDs[0];

		gl.glBindTexture(GL_TEXTURE_3D, textureID);

		gl.glTexStorage3D(GL_TEXTURE_3D, 1, GL_RGBA8, noiseWidth, noiseHeight, noiseDepth);
		gl.glTexSubImage3D(GL_TEXTURE_3D, 0, 0, 0, 0,
				noiseWidth, noiseHeight, noiseDepth, GL_RGBA, GL_UNSIGNED_INT_8_8_8_8_REV, bb);
		
		gl.glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

		return textureID;
	}
	
	void generateNoise()
	{	for (int x=0; x<noiseHeight; x++)
		{	for (int y=0; y<noiseWidth; y++)
			{	for (int z=0; z<noiseDepth; z++)
				{	noise[x][y][z] = random.nextDouble();
	}	}	}	}
	
	double smoothNoise(double x1, double y1, double z1)
	{	//get fractional part of x, y, and z
		double fractX = x1 - (int) x1;
		double fractY = y1 - (int) y1;
		double fractZ = z1 - (int) z1;

		//neighbor values
		int x2 = ((int)x1 + noiseWidth - 1) % noiseWidth;
		int y2 = ((int)y1 + noiseHeight- 1) % noiseHeight;
		int z2 = ((int)z1 + noiseDepth - 1) % noiseDepth;

		//smooth the noise by interpolating
		double value = 0.0;
		value += fractX     * fractY     * fractZ     * noise[(int)x1][(int)y1][(int)z1];
		value += fractX     * (1-fractY) * fractZ     * noise[(int)x1][(int)y2][(int)z1];
		value += (1-fractX) * fractY     * fractZ     * noise[(int)x2][(int)y1][(int)z1];
		value += (1-fractX) * (1-fractY) * fractZ     * noise[(int)x2][(int)y2][(int)z1];

		value += fractX     * fractY     * (1-fractZ) * noise[(int)x1][(int)y1][(int)z2];
		value += fractX     * (1-fractY) * (1-fractZ) * noise[(int)x1][(int)y2][(int)z2];
		value += (1-fractX) * fractY     * (1-fractZ) * noise[(int)x2][(int)y1][(int)z2];
		value += (1-fractX) * (1-fractY) * (1-fractZ) * noise[(int)x2][(int)y2][(int)z2];

		return value;
	}

	private double turbulence(double x, double y, double z, double size)
	{	double value = 0.0, initialSize = size;
		while(size >= 0.9)
		{	value = value + smoothNoise(x/size, y/size, z/size) * size;
			size = size / 2.0;
		}
		value = 128.0 * value / initialSize;
		return value;
	}
	public void setupShadowBuffers()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		scSizeX = myCanvas.getWidth();
		scSizeY = myCanvas.getHeight();
	
		gl.glGenFramebuffers(1, shadow_buffer, 0);
	
		gl.glGenTextures(1, shadow_tex, 0);
		gl.glBindTexture(GL_TEXTURE_2D, shadow_tex[0]);
		gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32,
						scSizeX, scSizeY, 0, GL_DEPTH_COMPONENT, GL_FLOAT, null);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_COMPARE_REF_TO_TEXTURE);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);
		
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
	}

// -----------------------------
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		setupShadowBuffers();
	}

	private void setupVertices()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
	

		Vertex3D[] torus_vertices = myTorus.getVertices();
		
		int[] torus_indices = myTorus.getIndices();	
		float[] torus_fvalues = new float[torus_indices.length*3];
		float[] torus_nvalues = new float[torus_indices.length*3];
		
		for (int i=0; i<torus_indices.length; i++)
		{	torus_fvalues[i*3]   = (float) (torus_vertices[torus_indices[i]]).getX();			
			torus_fvalues[i*3+1] = (float) (torus_vertices[torus_indices[i]]).getY();
			torus_fvalues[i*3+2] = (float) (torus_vertices[torus_indices[i]]).getZ();
			
			torus_nvalues[i*3]   = (float) (torus_vertices[torus_indices[i]]).getNormalX();
			torus_nvalues[i*3+1] = (float) (torus_vertices[torus_indices[i]]).getNormalY();
			torus_nvalues[i*3+2] = (float) (torus_vertices[torus_indices[i]]).getNormalZ();
		}
		numTorusVertices = torus_indices.length;
				

		
		Vertex3D[] house_vertices = house.getVertices();
		numHouseVertices = house.getNumVertices();

		float[] house_vertex_positions = new float[numHouseVertices*3];
		float[] house_normals = new float[numHouseVertices*3];
		float[] house_texture_coordinates = new float[numHouseVertices*2];
		for (int i=0; i<numHouseVertices; i++)
		{	house_vertex_positions[i*3]   = (float) (house_vertices[i]).getX();			
			house_vertex_positions[i*3+1] = (float) (house_vertices[i]).getY();
			house_vertex_positions[i*3+2] = (float) (house_vertices[i]).getZ();
			
			house_normals[i*3]   = (float) (house_vertices[i]).getNormalX();
			house_normals[i*3+1] = (float) (house_vertices[i]).getNormalY();
			house_normals[i*3+2] = (float) (house_vertices[i]).getNormalZ();
			
			house_texture_coordinates[i*2] = (float) (house_vertices[i]).getS();
			house_texture_coordinates[i*2+1] = (float) (house_vertices[i]).getT();
		}
		
		Vertex3D[] cube_vertices = cube.getVertices();
		numCubeVertices = cube.getNumVertices();

		float[] cube_vertex_positions = new float[numCubeVertices*3];
		float[] cube_normals = new float[numCubeVertices*3];
		float[] cube_texture_coordinates = new float[numCubeVertices*2];
		for (int i=0; i<numCubeVertices; i++)
		{	cube_vertex_positions[i*3]   = (float) (cube_vertices[i]).getX();			
			cube_vertex_positions[i*3+1] = (float) (cube_vertices[i]).getY();
			cube_vertex_positions[i*3+2] = (float) (cube_vertices[i]).getZ();
			
			cube_normals[i*3]   = (float) (cube_vertices[i]).getNormalX();
			cube_normals[i*3+1] = (float) (cube_vertices[i]).getNormalY();
			cube_normals[i*3+2] = (float) (cube_vertices[i]).getNormalZ();
			
			cube_texture_coordinates[i*2] = (float) (cube_vertices[i]).getS();
			cube_texture_coordinates[i*2+1] = (float) (cube_vertices[i]).getT();
		}
		
		Vertex3D[] woodStack_vertices = woodStack.getVertices();
		numWoodStackVertices = woodStack.getNumVertices();

		float[] woodStack_vertex_positions = new float[numWoodStackVertices*3];
		float[] woodStack_normals = new float[numWoodStackVertices*3];
		float[] woodStack_texture_coordinates = new float[numWoodStackVertices*2];
		for (int i=0; i<numWoodStackVertices; i++)
		{	woodStack_vertex_positions[i*3]   = (float) (woodStack_vertices[i]).getX();			
			woodStack_vertex_positions[i*3+1] = (float) (woodStack_vertices[i]).getY();
			woodStack_vertex_positions[i*3+2] = (float) (woodStack_vertices[i]).getZ();
			
			woodStack_normals[i*3]   = (float) (woodStack_vertices[i]).getNormalX();
			woodStack_normals[i*3+1] = (float) (woodStack_vertices[i]).getNormalY();
			woodStack_normals[i*3+2] = (float) (woodStack_vertices[i]).getNormalZ();
			
			woodStack_texture_coordinates[i*2] = (float) (woodStack_vertices[i]).getS();
			woodStack_texture_coordinates[i*2+1] = (float) (woodStack_vertices[i]).getT();
		}
		
		
		
		float [] axis_positions = 
			{ 
				0.0f, 0.0f, 0.0f,
				0.0f, 1.0f, 0.0f,
				
				0.0f, 0.0f, 0.0f,
				1.0f, 0.0f, 0.0f,
				
				0.0f, 0.0f, 0.0f,
				0.0f, 0.0f, 1.0f
			};
		
		float[] cube_map_vertices =
	        {	-1.0f,  1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f,
			1.0f, -1.0f, -1.0f, 1.0f,  1.0f, -1.0f, -1.0f,  1.0f, -1.0f,
			1.0f, -1.0f, -1.0f, 1.0f, -1.0f,  1.0f, 1.0f,  1.0f, -1.0f,
			1.0f, -1.0f,  1.0f, 1.0f,  1.0f,  1.0f, 1.0f,  1.0f, -1.0f,
			1.0f, -1.0f,  1.0f, -1.0f, -1.0f,  1.0f, 1.0f,  1.0f,  1.0f,
			-1.0f, -1.0f,  1.0f, -1.0f,  1.0f,  1.0f, 1.0f,  1.0f,  1.0f,
			-1.0f, -1.0f,  1.0f, -1.0f, -1.0f, -1.0f, -1.0f,  1.0f,  1.0f,
			-1.0f, -1.0f, -1.0f, -1.0f,  1.0f, -1.0f, -1.0f,  1.0f,  1.0f,
			-1.0f, -1.0f,  1.0f,  1.0f, -1.0f,  1.0f,  1.0f, -1.0f, -1.0f,
			1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f,  1.0f,
			-1.0f,  1.0f, -1.0f, 1.0f,  1.0f, -1.0f, 1.0f,  1.0f,  1.0f,
			1.0f,  1.0f,  1.0f, -1.0f,  1.0f,  1.0f, -1.0f,  1.0f, -1.0f
		};

		

		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);

		gl.glGenBuffers(vbo.length, vbo, 0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		FloatBuffer vertBuf = Buffers.newDirectFloatBuffer(torus_fvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit()*4, vertBuf, GL_STATIC_DRAW);
		

		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
		FloatBuffer torusNorBuf = Buffers.newDirectFloatBuffer(torus_nvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, torusNorBuf.limit()*4, torusNorBuf, GL_STATIC_DRAW);
		

		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[4]);
		FloatBuffer houseVertBuf = Buffers.newDirectFloatBuffer(house_vertex_positions);
		gl.glBufferData(GL_ARRAY_BUFFER, houseVertBuf.limit()*4, houseVertBuf, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[5]);
		FloatBuffer houseNorBuf = Buffers.newDirectFloatBuffer(house_normals);
		gl.glBufferData(GL_ARRAY_BUFFER, houseNorBuf.limit()*4, houseNorBuf, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[6]);
		FloatBuffer houseTexBuf = Buffers.newDirectFloatBuffer(house_texture_coordinates);
		gl.glBufferData(GL_ARRAY_BUFFER, houseTexBuf.limit()*4, houseTexBuf, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[7]);
		FloatBuffer lightCubeV = Buffers.newDirectFloatBuffer(cube_vertex_positions);
		gl.glBufferData(GL_ARRAY_BUFFER, lightCubeV.limit()*4, lightCubeV, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[8]);
		FloatBuffer lightCubeN = Buffers.newDirectFloatBuffer(cube_normals);
		gl.glBufferData(GL_ARRAY_BUFFER, lightCubeN.limit()*4, lightCubeN, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[9]);
		FloatBuffer cubeTexBuf = Buffers.newDirectFloatBuffer(cube_texture_coordinates);
		gl.glBufferData(GL_ARRAY_BUFFER, cubeTexBuf.limit()*4, cubeTexBuf, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[10]);
		FloatBuffer axisBuffer = Buffers.newDirectFloatBuffer(axis_positions);
		gl.glBufferData(GL_ARRAY_BUFFER, axisBuffer.limit()*4, axisBuffer, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[11]);
		FloatBuffer woodStackV = Buffers.newDirectFloatBuffer(woodStack_vertex_positions);
		gl.glBufferData(GL_ARRAY_BUFFER, woodStackV.limit()*4, woodStackV, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[12]);
		FloatBuffer woodStackN = Buffers.newDirectFloatBuffer(woodStack_normals);
		gl.glBufferData(GL_ARRAY_BUFFER, woodStackN.limit()*4, woodStackN, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[13]);
		FloatBuffer woodStackT = Buffers.newDirectFloatBuffer(woodStack_texture_coordinates);
		gl.glBufferData(GL_ARRAY_BUFFER, woodStackT.limit()*4, woodStackT, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[14]);
		FloatBuffer cubeVertBuf = Buffers.newDirectFloatBuffer(cube_map_vertices);
		gl.glBufferData(GL_ARRAY_BUFFER, cubeVertBuf.limit()*4, cubeVertBuf, GL_STATIC_DRAW);
		
	}
	
	private void installLights(int rendering_program,PositionalLight currLight, Matrix3D v_matrix)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
	
		Material currentMaterial = new Material();
		currLight = currentLight;
		currentMaterial = thisMaterial;
		Point3D lightP = currentLight.getPosition();
		Point3D lightPv = lightP.mult(v_matrix);
		
		float [] currLightPos = new float[] { (float) lightPv.getX(),
			(float) lightPv.getY(),
			(float) lightPv.getZ() };

		// get the location of the global ambient light field in the shader
		int globalAmbLoc = gl.glGetUniformLocation(rendering_program, "globalAmbient");
	
		// set the current globalAmbient settings
		gl.glProgramUniform4fv(rendering_program, globalAmbLoc, 1, globalAmbient, 0);

		// get the locations of the light and material fields in the shader
		int ambLoc = gl.glGetUniformLocation(rendering_program, "light.ambient");
		int diffLoc = gl.glGetUniformLocation(rendering_program, "light.diffuse");
		int specLoc = gl.glGetUniformLocation(rendering_program, "light.specular");
		int posLoc = gl.glGetUniformLocation(rendering_program, "light.position");

		int MambLoc = gl.glGetUniformLocation(rendering_program, "material.ambient");
		int MdiffLoc = gl.glGetUniformLocation(rendering_program, "material.diffuse");
		int MspecLoc = gl.glGetUniformLocation(rendering_program, "material.specular");
		int MshiLoc = gl.glGetUniformLocation(rendering_program, "material.shininess");

		// set the uniform light and material values in the shader
		gl.glProgramUniform4fv(rendering_program, ambLoc, 1, currLight.getAmbient(), 0);
		gl.glProgramUniform4fv(rendering_program, diffLoc, 1, currLight.getDiffuse(), 0);
		gl.glProgramUniform4fv(rendering_program, specLoc, 1, currLight.getSpecular(), 0);
		gl.glProgramUniform3fv(rendering_program, posLoc, 1, currLightPos, 0);
	
		gl.glProgramUniform4fv(rendering_program, MambLoc, 1, currentMaterial.getAmbient(), 0);
		gl.glProgramUniform4fv(rendering_program, MdiffLoc, 1, currentMaterial.getDiffuse(), 0);
		gl.glProgramUniform4fv(rendering_program, MspecLoc, 1, currentMaterial.getSpecular(), 0);
		gl.glProgramUniform1f(rendering_program, MshiLoc, currentMaterial.getShininess());
	}

	public static void main(String[] args) { new Code(); }

	@Override
	public void dispose(GLAutoDrawable drawable)
	{	GL4 gl = (GL4) drawable.getGL();
		gl.glDeleteVertexArrays(1, vao, 0);
	}

//-----------------
	private void createShaderPrograms()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		int[] vertCompiled = new int[1];
		int[] fragCompiled = new int[1];

		vBlinn1ShaderSource = util.readShaderSource("shaders/blinnVert1.shader");
		vBlinn2ShaderSource = util.readShaderSource("shaders/blinnVert2.shader");
		fBlinn2ShaderSource = util.readShaderSource("shaders/blinnFrag2.shader");
		vertexShaderSource = util.readShaderSource("shaders/vertex.shader");
		fragmentShaderSource = util.readShaderSource("shaders/fragment.shader");
		vertexTShaderSource = util.readShaderSource("shaders/vertexT.shader");
		fragmentTShaderSource = util.readShaderSource("shaders/fragmentT.shader");
		tessEShaderSource = util.readShaderSource("shaders/tessE.shader");
		tessCShaderSource = util.readShaderSource("shaders/tessC.shader");
		
		int vertexShader1 = gl.glCreateShader(GL_VERTEX_SHADER);
		int vertexShader2 = gl.glCreateShader(GL_VERTEX_SHADER);
		int fragmentShader2 = gl.glCreateShader(GL_FRAGMENT_SHADER);
		int vertexShader = gl.glCreateShader(GL_VERTEX_SHADER);
		int fragmentShader = gl.glCreateShader(GL_FRAGMENT_SHADER);
		int vShader  = gl.glCreateShader(GL_VERTEX_SHADER);
		int tcShader = gl.glCreateShader(GL_TESS_CONTROL_SHADER);
		int teShader = gl.glCreateShader(GL_TESS_EVALUATION_SHADER);
		int fShader  = gl.glCreateShader(GL_FRAGMENT_SHADER);
		
		gl.glShaderSource(vertexShader1, vBlinn1ShaderSource.length, vBlinn1ShaderSource, null, 0);
		gl.glShaderSource(vertexShader2, vBlinn2ShaderSource.length, vBlinn2ShaderSource, null, 0);
		gl.glShaderSource(fragmentShader2, fBlinn2ShaderSource.length, fBlinn2ShaderSource, null, 0);
		gl.glShaderSource(vertexShader, vertexShaderSource.length, vertexShaderSource, null,0);
		gl.glShaderSource(fragmentShader, fragmentShaderSource.length, fragmentShaderSource, null,0);
		gl.glShaderSource(vShader, vertexTShaderSource.length, vertexTShaderSource, null, 0);
		gl.glShaderSource(tcShader, tessCShaderSource.length, tessCShaderSource, null, 0);
		gl.glShaderSource(teShader, tessEShaderSource.length, tessEShaderSource, null, 0);
		gl.glShaderSource(fShader, fragmentTShaderSource.length, fragmentTShaderSource, null, 0);

		gl.glCompileShader(vertexShader1);
		gl.glCompileShader(vertexShader2);
		gl.glCompileShader(fragmentShader2);
		gl.glCompileShader(vertexShader);
		gl.glCompileShader(fragmentShader);

		gl.glCompileShader(vShader);
		gl.glCompileShader(tcShader);
		gl.glCompileShader(teShader);
		gl.glCompileShader(fShader);
		
		rendering_program1 = gl.glCreateProgram();
		rendering_program2 = gl.glCreateProgram();
		rendering_program3 = gl.glCreateProgram();
		rendering_program4 = gl.glCreateProgram();
		gl.glAttachShader(rendering_program1, vertexShader1);
		gl.glAttachShader(rendering_program2, vertexShader2);
		gl.glAttachShader(rendering_program2, fragmentShader2);
		gl.glAttachShader(rendering_program3, vertexShader);
		gl.glAttachShader(rendering_program3, fragmentShader);
		gl.glAttachShader(rendering_program4, vShader);
		gl.glAttachShader(rendering_program4, tcShader);
		gl.glAttachShader(rendering_program4, teShader);
		gl.glAttachShader(rendering_program4, fShader);

		gl.glLinkProgram(rendering_program1);
		gl.glLinkProgram(rendering_program2);
		gl.glLinkProgram(rendering_program3);
		gl.glLinkProgram(rendering_program4);
		
		//CUBE MAP RENDERING PROGRAM
		
		cubeVertShaderSource = util.readShaderSource("shaders/vertCube.shader");
		cubeFragShaderSource = util.readShaderSource("shaders/fragCube.shader");

		int cubeVertexShader = gl.glCreateShader(GL_VERTEX_SHADER);
		int cubeFragmentShader = gl.glCreateShader(GL_FRAGMENT_SHADER);

		gl.glShaderSource(cubeVertexShader, cubeVertShaderSource.length, cubeVertShaderSource, null, 0);
		gl.glShaderSource(cubeFragmentShader, cubeFragShaderSource.length, cubeFragShaderSource, null, 0);

		gl.glCompileShader(cubeVertexShader);
		gl.glCompileShader(cubeFragmentShader);

		rendering_program_cube_map = gl.glCreateProgram();
		gl.glAttachShader(rendering_program_cube_map, cubeVertexShader);
		gl.glAttachShader(rendering_program_cube_map, cubeFragmentShader);
		gl.glLinkProgram(rendering_program_cube_map);
		
	}

	
	
	private int loadCubeMap()
	{
		GL4 gl = (GL4) GLContext.getCurrentGL();
		GLProfile glp = gl.getGLProfile();
		Texture tex = new Texture(GL_TEXTURE_CUBE_MAP);
		
		try {
			
			TextureData topFile = TextureIO.newTextureData(glp, new File("cubeMap/top.jpg"), false, "jpg");
			TextureData leftFile = TextureIO.newTextureData(glp, new File("cubeMap/left.jpg"), false, "jpg");
			TextureData fntFile = TextureIO.newTextureData(glp, new File("cubeMap/center.jpg"), false, "jpg");
			TextureData rightFile = TextureIO.newTextureData(glp, new File("cubeMap/right.jpg"), false, "jpg");
			TextureData bkFile = TextureIO.newTextureData(glp, new File("cubeMap/back.jpg"), false, "jpg");
			TextureData botFile = TextureIO.newTextureData(glp, new File("cubeMap/bottom.jpg"), false, "jpg");
			tex.updateImage(gl, rightFile, GL_TEXTURE_CUBE_MAP_POSITIVE_X);
			tex.updateImage(gl, leftFile, GL_TEXTURE_CUBE_MAP_NEGATIVE_X);
			tex.updateImage(gl, botFile, GL_TEXTURE_CUBE_MAP_POSITIVE_Y);
			tex.updateImage(gl, topFile, GL_TEXTURE_CUBE_MAP_NEGATIVE_Y);
			tex.updateImage(gl, fntFile, GL_TEXTURE_CUBE_MAP_POSITIVE_Z);
			tex.updateImage(gl, bkFile, GL_TEXTURE_CUBE_MAP_NEGATIVE_Z);
		
		} catch (IOException|GLException e) {System.out.println("DIDNT WORK"); }
		
		int[] textureIDs = new int[1];
		gl.glGenTextures(1, textureIDs, 0);
		int textureID = tex.getTextureObject();
		
		// reduce seams
		gl.glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
		
		return textureID;
	}
//------------------
	private Matrix3D perspective(float fovy, float aspect, float n, float f)
	{	float q = 1.0f / ((float) Math.tan(Math.toRadians(0.5f * fovy)));
		float A = q / aspect;
		float B = (n + f) / (n - f);
		float C = (2.0f * n * f) / (n - f);
		Matrix3D r = new Matrix3D();
		r.setElementAt(0,0,A);
		r.setElementAt(1,1,q);
		r.setElementAt(2,2,B);
		r.setElementAt(3,2,-1.0f);
		r.setElementAt(2,3,C);
		r.setElementAt(3,3,0.0f);
		return r;
	}

	
	
	private Matrix3D lookAt(Point3D eye, Point3D target, Vector3D y)
	{	Vector3D eyeV = new Vector3D(eye);
		Vector3D targetV = new Vector3D(target);
		Vector3D fwd = (targetV.minus(eyeV)).normalize();
		Vector3D side = (fwd.cross(y)).normalize();
		Vector3D up = (side.cross(fwd)).normalize();
		Matrix3D look = new Matrix3D();
		look.setElementAt(0,0, side.getX());
		look.setElementAt(1,0, up.getX());
		look.setElementAt(2,0, -fwd.getX());
		look.setElementAt(3,0, 0.0f);
		look.setElementAt(0,1, side.getY());
		look.setElementAt(1,1, up.getY());
		look.setElementAt(2,1, -fwd.getY());
		look.setElementAt(3,1, 0.0f);
		look.setElementAt(0,2, side.getZ());
		look.setElementAt(1,2, up.getZ());
		look.setElementAt(2,2, -fwd.getZ());
		look.setElementAt(3,2, 0.0f);
		look.setElementAt(0,3, side.dot(eyeV.mult(-1)));
		look.setElementAt(1,3, up.dot(eyeV.mult(-1)));
		look.setElementAt(2,3, (fwd.mult(-1)).dot(eyeV.mult(-1)));
		look.setElementAt(3,3, 1.0f);
		return(look);
	}
	
	public Texture loadTexture(String textureFileName)
	{	Texture tex = null;
		try { tex = TextureIO.newTexture(new File(textureFileName), false); }
		catch (Exception e) { e.printStackTrace(); }
		return tex;
	}

	public void defineMaterials()

	{
		//obsidian Material
		Material m = new Material();
		float[] amb = new float[]{.005475f, .05f, .006625f, 1.0f};
		float[] dif = new float[]{.18275f, .17f, .22525f, 1.0f};
		float[] spec = new float[]{.332741f, .328634f, .346435f, 1.0f};
		m.setAmbient(amb);
		m.setDiffuse(dif);
		m.setSpecular(spec);
		m.setShininess(38.4f);
		materialObsidian = m;
		 m = new Material();
		 amb = new float[]{0.5f, 0.35f, 0.15f, 1.0f};
		 dif = new float[]{0.5f, 0.35f, 0.15f, 1.0f};
		spec = new float[]{0.5f, 0.35f, 0.15f, 1.0f};
		m.setAmbient(amb);
		m.setDiffuse(dif);
		m.setSpecular(spec);
		m.setShininess(15.0f);
		materialWood = m;

	}
	
	public void setupKeys()
	{
		KeyStroke spaceKey = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0);
		int map = JComponent.WHEN_IN_FOCUSED_WINDOW;
		InputMap iMap = this.getRootPane().getInputMap(map);
		iMap.put(spaceKey, "Space");
		ActionMap aMap = this.getRootPane().getActionMap();
		
		aMap.put("Space", new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				
				showAxes = !showAxes;
			}
		});
		
		//A Key
		KeyStroke AKey = KeyStroke.getKeyStroke(KeyEvent.VK_A, 0);
		iMap.put(AKey, "A");
		aMap.put("A", new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				cameraLoc.setX(cameraLoc.getX() - .2f);
			}
		});
		
		//D Key
		KeyStroke DKey = KeyStroke.getKeyStroke(KeyEvent.VK_D, 0);
		iMap.put(DKey, "D");
		aMap.put("D", new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				cameraLoc.setX(cameraLoc.getX()+ .2f);
			}
		});
		
		//S Key
		KeyStroke SKey = KeyStroke.getKeyStroke(KeyEvent.VK_S, 0);
		iMap.put(SKey, "S");
		aMap.put("S", new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				cameraLoc.setZ(cameraLoc.getZ() + .2f);
			}
		});
		
		//W Key
		KeyStroke WKey = KeyStroke.getKeyStroke(KeyEvent.VK_W, 0);
		iMap.put(WKey, "W");
		aMap.put("W", new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				cameraLoc.setZ(cameraLoc.getZ() - .2f);
				
			}
		});
		
		//Q Key
		KeyStroke QKey = KeyStroke.getKeyStroke(KeyEvent.VK_Q, 0);
		iMap.put(QKey, "Q");
		aMap.put("Q", new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				cameraLoc.setY(cameraLoc.getY() + .2f);
			}
		});
		
		//E Key
		KeyStroke EKey = KeyStroke.getKeyStroke(KeyEvent.VK_E, 0);
		iMap.put(EKey, "E");
		aMap.put("E", new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				cameraLoc.setY(cameraLoc.getY() - .2f);
			}
		});
		
		
		//Arrow Up Key
		KeyStroke iKey = KeyStroke.getKeyStroke(KeyEvent.VK_I, 0);
		iMap.put(iKey, "i");
		aMap.put("i", new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				degrees = degrees -2;
				
				System.out.println(v_matrix);
				
			}
		});
		
		KeyStroke kKey = KeyStroke.getKeyStroke(KeyEvent.VK_K, 0);
		iMap.put(kKey, "k");
		aMap.put("k", new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				degrees = degrees + 2;
				//upAxis = new Vector3D(1, 0, 0);
			
				
			}
		});
		
		KeyStroke jKey = KeyStroke.getKeyStroke(KeyEvent.VK_J, 0);
		iMap.put(jKey, "j");
		aMap.put("j", new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				degrees2 = degrees2 - 2;
				//upAxis = new Vector3D(0, 1, 0);

				
			}
		});
		
		KeyStroke lKey = KeyStroke.getKeyStroke(KeyEvent.VK_L, 0);
		iMap.put(lKey, "l");
		aMap.put("l", new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				degrees2 = degrees2 + 2;
				//cameraLoc.setX(cameraLoc.getX() + 1);
			
				
			}
		});
		
		KeyStroke BKey = KeyStroke.getKeyStroke(KeyEvent.VK_B, 0);
		iMap.put(BKey, "B");
		aMap.put("B", new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				lightActive = !lightActive;

			}
		});


	}
	public void mouseClicked(MouseEvent arg0) {
		
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if(lightActive)
		{
			int moved = e.getWheelRotation();
			if(moved < 0)
			{
				
				lightLoc.setZ(lightLoc.getZ() - 1);
	
			}
			else
			{
				
				lightLoc.setZ(lightLoc.getZ() +1 );
			}

		}
		
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		//System.out.println(e.getX());
		if(lightActive)
		{
			currMouseMovedX = e.getX();
			currMouseMovedY = e.getY();
			
			double x = pointLookAt.getX();
			double y = pointLookAt.getY();
			double z = pointLookAt.getZ();
			
			if(currMouseMovedX < prevMouseMovedX && currMouseMovedX != prevMouseMovedX)
			{
				lightLoc.setX(lightLoc.getX() - .1f);
			}
			else if (currMouseMovedX > prevMouseMovedX && currMouseMovedX != prevMouseMovedX)
			{
				lightLoc.setX(lightLoc.getX() + .1f);
			}
			if(currMouseMovedY > prevMouseMovedY && currMouseMovedY != prevMouseMovedY)
			{
				lightLoc.setY(lightLoc.getY() - .1f);
			}
			else if (currMouseMovedY < prevMouseMovedY && currMouseMovedY != prevMouseMovedY)
			{
				lightLoc.setY(lightLoc.getY() + .1f);
			}
			prevMouseMovedX = currMouseMovedX;
			prevMouseMovedY = currMouseMovedY;
			System.out.println(lightLoc);
			if(lightLoc.getX() > 5.5)
			{
				
				pointLookAt = new Point3D(4,0,1.1); 
			}
			else
			{

				pointLookAt = new Point3D(8, 0f, 1.1f);
			}
		
		}
		
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	
		
}
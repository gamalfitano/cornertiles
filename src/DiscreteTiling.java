import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWKeyCallback;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL.createCapabilities;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_BGR;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL21.GL_SRGB8;
import static org.lwjgl.opengl.GL30.*;

import Math.*;

public class DiscreteTiling {

    // Amount of columns times the size of a float since the values are float values
    public static int MAGICNUMBER123 = 44;

    // SCENE 1 Basic Test Scene | SCENE 2 Display Scene to see the texture atlas
    public static int SCENE = 1;

    public static Vec3 boxPosition = new Vec3(0, 0, 0);
    public static Vec3 boxPosition2 = new Vec3(0, 0, 0);
    public static Vec3 cameraPosition = new Vec3(0, -0, -150);

    public static void main(String[] args) throws Exception {
        // let GLFW work on the main thread (for OS X)
        // read the following if you want to create windows with awt/swing/javaFX:
        // https://stackoverflow.com/questions/47006058/lwjgl-java-awt-headlessexception-thrown-when-making-a-jframe
        System.setProperty("java.awt.headless", "true");

        // open a window
        GLFWErrorCallback.createPrint(System.err).set();
        if (!GLFW.glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 4);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 0);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);

        long hWindow = GLFW.glfwCreateWindow(1920, 1080, "CornerTiles", 0, 0);
        // https://www.glfw.org/docs/3.3.2/input_guide.html#input_keyboard
        GLFWKeyCallback keyCallback;
        GLFW.glfwSetKeyCallback(hWindow, keyCallback = GLFWKeyCallback.create((window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_UP) {
                System.out.println("SHIFT W PRESSED W Pressed");
                cameraPosition = new Vec3(cameraPosition.x, cameraPosition.y + 1, cameraPosition.z);
            } else if (key == GLFW_KEY_DOWN) {
                System.out.println("S Pressed");
                cameraPosition = new Vec3(cameraPosition.x, cameraPosition.y - 1, cameraPosition.z);
            } else if (key == GLFW_KEY_LEFT) {
                System.out.println("A Pressed");
                cameraPosition = new Vec3(cameraPosition.x + 1, cameraPosition.y, cameraPosition.z);
            } else if (key == GLFW_KEY_RIGHT) {
                System.out.println("D Pressed");
                cameraPosition = new Vec3(cameraPosition.x - 1, cameraPosition.y, cameraPosition.z);
            } else if (key == GLFW_KEY_PAGE_UP) {
                System.out.println("Q Pressed");
                cameraPosition = new Vec3(cameraPosition.x, cameraPosition.y, cameraPosition.z + 1);
            } else if (key == GLFW_KEY_PAGE_DOWN) {
                System.out.println("E Pressed");
                cameraPosition = new Vec3(cameraPosition.x, cameraPosition.y, cameraPosition.z - 1);
            } else if (key == GLFW_KEY_W) {
                System.out.println("W Pressed");
                boxPosition = new Vec3(boxPosition.x, boxPosition.y + 1, boxPosition.z);
            } else if (key == GLFW_KEY_S) {
                System.out.println("S Pressed");
                boxPosition = new Vec3(boxPosition.x, boxPosition.y - 1, boxPosition.z);
            } else if (key == GLFW_KEY_A) {
                System.out.println("A Pressed");
                boxPosition = new Vec3(boxPosition.x + 1, boxPosition.y, boxPosition.z);
            } else if (key == GLFW_KEY_D) {
                System.out.println("D Pressed");
                boxPosition = new Vec3(boxPosition.x - 1, boxPosition.y, boxPosition.z);
            } else if (key == GLFW_KEY_Q) {
                System.out.println("Q Pressed");
                boxPosition = new Vec3(boxPosition.x, boxPosition.y, boxPosition.z + 1);
            } else if (key == GLFW_KEY_E) {
                System.out.println("E Pressed");
                boxPosition = new Vec3(boxPosition.x, boxPosition.y, boxPosition.z - 1);
            } else if (key == GLFW_KEY_P) {
                boxPosition2 = new Vec3(1000, 1000, -1000);
                System.out.println("P Pressed");
            } else if (key == GLFW_KEY_O) {
                System.out.println("X : " + boxPosition.x + " Y : " + boxPosition.y + " Z : " + boxPosition.z);
                System.out.println("Camera X : " + cameraPosition.x + " Camera Y : " + cameraPosition.y + "Camera Z : " + cameraPosition.z);
            } else if (key == GLFW_KEY_I) {
                boxPosition = new Vec3(boxPosition.x, 51, boxPosition.z);
            } else if (key == GLFW_KEY_U) {
                boxPosition2 = new Vec3(0, 0, -10);
            }
        }));
        GLFW.glfwSetWindowSizeCallback(hWindow, (window, width, height) -> {
            int[] w = new int[1];
            int[] h = new int[1];
            GLFW.glfwGetFramebufferSize(window, w, h);
            glViewport(0, 0, w[0], h[0]);
        });
        GLFW.glfwMakeContextCurrent(hWindow);
        GLFW.glfwSwapInterval(1);
        createCapabilities();

        // set up opengl
        glEnable(GL_FRAMEBUFFER_SRGB);
        glClearColor(0.6f, 0.6f, 0.6f, 0.0f);
        // glClearDepth(1);
        // glDisable(GL_DEPTH_TEST);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LESS);
        glDepthRange(0, 2);
        // glDisable(GL_CULL_FACE);

        // load, compile and link shaders
        // see https://www.khronos.org/opengl/wiki/Vertex_Shader
        String VertexShaderSource =
                "#version 400 core\n"
                        + "\n"
                        + "in vec2 tex;\n"
                        + "in vec3 pos;\n"
                        + "in vec3 col;\n"
                        + "in vec3 norm;\n"
                        + "uniform vec3 posLight;\n"
                        + "uniform vec3 posCam;\n"
                        + "uniform mat4 mvp;\n"
                        + "out vec2 texturePosition;\n"
                        + "out vec3 colorIn;\n"
                        + "out vec3 toLight;\n"
                        + "out vec3 toCam;\n"
                        + "out vec3 surfaceNormal;\n"
                        + "\n"
                        + "void main()\n"
                        + "{\n"
                        + "  vec4 worldCoordinates = mvp * vec4(pos, 1);\n"
                        + "  gl_Position = mvp * vec4(pos,1); \n"
                        + "  texturePosition = tex; \n"
                        + "  colorIn = col; \n"
                        + "  toLight = posLight - worldCoordinates.xyz;\n"
                        + "  toCam = posCam - worldCoordinates.xyz;\n"
                        + "  surfaceNormal = (mvp * vec4(norm,0)).xyz;\n"
                        + "}";
        int hVertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(hVertexShader, VertexShaderSource);
        glCompileShader(hVertexShader);
        if (glGetShaderi(hVertexShader, GL_COMPILE_STATUS) != GL_TRUE)
            throw new Exception(glGetShaderInfoLog(hVertexShader));

        // see https://www.khronos.org/opengl/wiki/Fragment_Shader


        String FragmentShaderSource =
                "#version 400 core\n"
                        + "\n"
                        + "in vec2 texturePosition;\n"
                        + "in vec3 colorIn;\n"
                        + "in vec3 surfaceNormal;\n"
                        + "in vec3 toLight;\n"
                        + "in vec3 toCam;\n"

                        + "uniform float diamond;\n"
                        + "uniform sampler2D aTexture;\n"
                        + "uniform vec3 colLight;\n"
                        + "uniform int iterations;\n"
                        + "uniform vec4 WHITE;\n"
                        + "uniform vec4 BLACK;\n"

                        + "out vec4 color;\n"

                        + "\n"
                        + "void main()\n"
                        + "{\n"
                        + "  vec3 normSurfaceNormal = normalize(surfaceNormal);\n"
                        + "  vec3 normToLight= normalize(toLight);\n"
                        + "  vec3 normToCam = normalize(toCam);\n"

                        + "  float dotProd = dot(normSurfaceNormal , normToLight);\n"
                        + "  float brightness = max(dotProd,0);\n"
                        + "  vec3 diffuseColor = brightness * colLight;\n"
                        + "  vec3 lightDirection = -normToLight;\n"
                        + "  vec3 reflectionDirection = reflect(lightDirection,normSurfaceNormal);\n"
                        + "  float specular = max(dot(reflectionDirection, normToCam),0);\n"

                        + "  float specular2 = pow(specular, diamond);\n"
                        + "  vec3 specular3 = specular2 * colLight;\n"
                        + "  vec4 textureColor = texture(aTexture, texturePosition);\n"
                        + "  vec4 vertexColors = vec4(colorIn,1);\n"

                        + "  float temp = texturePosition.x;\n"
                        + "  float temp2 = texturePosition.y;\n"
                        + "  temp = temp * temp2 * 321499 / 83 ;\n"
                        + "  temp2 = (temp2 + sin(temp))*89321 ;\n"
                        + "  int temp3 = int (temp);\n"
                        + "  int temp4 = int (temp2);\n"
                        + "  textureColor = ((temp3 - temp4)%2 == 0) ? BLACK : WHITE;\n"

                        + "  color = vec4(diffuseColor,1) *  textureColor + vec4(specular3,1);\n"
                        + "}";

        int hFragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(hFragmentShader, FragmentShaderSource);
        glCompileShader(hFragmentShader);
        if (glGetShaderi(hFragmentShader, GL_COMPILE_STATUS) != GL_TRUE)
            throw new Exception(glGetShaderInfoLog(hFragmentShader));

        // link shaders to a program
        int hProgram = glCreateProgram();
        glAttachShader(hProgram, hFragmentShader);
        glAttachShader(hProgram, hVertexShader);
        glLinkProgram(hProgram);
        if (glGetProgrami(hProgram, GL_LINK_STATUS) != GL_TRUE)
            throw new Exception(glGetProgramInfoLog(hProgram));

        // upload model vertices to a vbo
        float[] triangleVertices = new float[]
                {
                        //i copied this from nick brönnimann since its very neat

                        // Coordinate  Colour Values     Texture Coordinates  Normal           Front Side
                        -1f, -1f, +1f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0, 0, +1, // 0 bottom left
                        +1f, +1f, +1f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0, 0, +1, // 1 top    right
                        -1f, +1f, +1f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0, 0, +1, // 2 top    left
                        +1f, -1f, +1f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0, 0, +1, // 3 bottom right
                        -1f, -1f, +1f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0, 0, +1, // 4 (0)
                        +1f, +1f, +1f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0, 0, +1, // 5 (1)
                        // Coordinate  Colour Values     Texture Coordinates  Normal Right Side
                        +1f, -1f, +1f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, +1, 0, 0, // 6 bottom left
                        +1f, +1f, -1f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, +1, 0, 0, // 7 top    right
                        +1f, +1f, +1f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, +1, 0, 0, // 8 top    left
                        +1f, -1f, -1f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, +1, 0, 0, // 9 bottom right
                        +1f, -1f, +1f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, +1, 0, 0, // 10(6)
                        +1f, +1f, -1f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, +1, 0, 0, // 11(7)
                        // Coordinate  Colour Values     Texture Coordinates  Normal Back Side
                        +1f, -1f, -1f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0, 0, -1, // 12 bottom left
                        -1f, +1f, -1f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0, 0, -1, // 13 top    right
                        +1f, +1f, -1f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0, 0, -1, // 14 top    left
                        -1f, -1f, -1f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0, 0, -1, // 15 bottom right
                        +1f, -1f, -1f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0, 0, -1, // 16(12)
                        -1f, +1f, -1f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0, 0, -1, // 17(13)
                        // Coordinate  Colour Values     Texture Coordinates  Normal Left Side
                        -1f, -1f, -1f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, -1, 0, 0, // 18 bottom left
                        -1f, +1f, +1f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, -1, 0, 0, // 19 top    right
                        -1f, +1f, -1f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f, -1, 0, 0, // 20 top    left
                        -1f, -1f, +1f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, -1, 0, 0, // 21 bottom right
                        -1f, -1f, -1f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, -1, 0, 0, // 22(18)
                        -1f, +1f, +1f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, -1, 0, 0, // 23(19)
                        // Coordinate  Colour Values     Texture Coordinates  Normal Top Side
                        -1f, +1f, +1f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0, +1, 0, // 24 bottom left
                        +1f, +1f, -1f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0, +1, 0, // 25 top    right
                        -1f, +1f, -1f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0, +1, 0, // 26 top    left
                        +1f, +1f, +1f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0, +1, 0, // 27 bottom right
                        -1f, +1f, +1f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0, +1, 0, // 28(24)
                        +1f, +1f, -1f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0, +1, 0, // 29(25)
                        // Coordinate  Colour Values     Texture Coordinates  Normal Bottom Side
                        -1f, -1f, -1f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0, -1, 0, // 30 bottom left
                        +1f, -1f, +1f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0, -1, 0, // 31 top    right
                        -1f, -1f, +1f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0, -1, 0, // 32 top    left
                        +1f, -1f, -1f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0, -1, 0, // 33 bottom right
                        -1f, -1f, -1f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0, -1, 0, // 34(30)
                        +1f, -1f, +1f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0, -1, 0  // 35(31)
                };


        int vboTriangleVertices = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboTriangleVertices);
        glBufferData(GL_ARRAY_BUFFER, triangleVertices, GL_STATIC_DRAW);


        // upload model indices to a vbo
        // this was as well copied from nick brönnimann
        int[] triangleIndices = new int[]{
                // Front left
                0, 1, 2,
                // Front right
                4, 3, 5,
                // Top right
                28, 27, 29,
                // Top left
                24, 25, 26,
                // Left top
                18, 19, 20,
                // Left bottom
                22, 21, 23,
                // Bottom left
                30, 31, 32,
                // Bottom right
                34, 33, 35,
                // Right bottom
                10, 9, 11,
                // Right top
                6, 7, 8,
                // Back right
                16, 15, 17,
                // Back left
                12, 13, 14
        };
        //---------------------------------------------------------------------------------------------------------------

        glUseProgram(hProgram);

        // Load the first texture
        BufferedImage image = getTextureData("Resource/preTextureSynthesis/seamless2.jpg");
        ByteBuffer texture = ByteBuffer.allocateDirect(image.getWidth() * image.getHeight() * 3);
        // we have to flip the rgb to bgr because for some reason we get a EXCEPTION_ACCESS_VIOLATION if we dont flip it. Not too sure why.
        texture.put(((DataBufferByte) image.getRaster().getDataBuffer()).getData()).flip();

        int hTexture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, hTexture);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_SRGB8, image.getWidth(), image.getHeight(), 0, GL_BGR, GL_UNSIGNED_BYTE, texture);
        glGenerateMipmap(GL_TEXTURE_2D);
        glActiveTexture(GL_TEXTURE0 + 1);
        glBindTexture(GL_TEXTURE_2D, hTexture);

        int textureUniformHandle = glGetUniformLocation(hProgram, "aTexture");
        if (textureUniformHandle != -1) glUniform1i(textureUniformHandle, 1);

        //--------------------------------------------------------------------------------------------------------------------------------------



        // Load the first texture
        BufferedImage bufferedImage  = getTextureData("Resource/syntheticCornerSmall.jpg");
        // BufferedImage bufferedImage = generateCornerTilesTexutre(data, 2, 500, 500);


        ByteBuffer texture3 = ByteBuffer.allocateDirect(bufferedImage.getWidth() * bufferedImage.getHeight() * 3);
        // we have to flip the rgb to bgr because for some reason we get a EXCEPTION_ACCESS_VIOLATION if we dont flip it. Not too sure why.
        texture3.put(((DataBufferByte) bufferedImage.getRaster().getDataBuffer()).getData()).flip();

        int hTexture3 = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, hTexture3);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_SRGB8, bufferedImage.getWidth(), bufferedImage.getHeight(), 0, GL_BGR, GL_UNSIGNED_BYTE, texture3);
        glGenerateMipmap(GL_TEXTURE_2D);
        glActiveTexture(GL_TEXTURE0 + 1);
        glBindTexture(GL_TEXTURE_2D, hTexture3);

        int textureUniformHandle3 = glGetUniformLocation(hProgram, "cTexture");
        if (textureUniformHandle3 != -1) glUniform1i(textureUniformHandle3, 1);



        //--------------------------------------------------------------------------------------------------------------------------------------

        // Load the first texture
        BufferedImage image2 = getTextureData("Resource/syntheticCornerSmall.jpg");
        ByteBuffer texture2 = ByteBuffer.allocateDirect(image2.getWidth() * image2.getHeight() * 3);
        // we have to flip the rgb to bgr because for some reason we get a EXCEPTION_ACCESS_VIOLATION if we dont flip it. Not too sure why.
        texture2.put(((DataBufferByte) image2.getRaster().getDataBuffer()).getData()).flip();

        int hTexture2 = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, hTexture2);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_SRGB8, image2.getWidth(), image2.getHeight(), 0, GL_BGR, GL_UNSIGNED_BYTE, texture2);
        glGenerateMipmap(GL_TEXTURE_2D);
        glActiveTexture(GL_TEXTURE0 + 1);
        glBindTexture(GL_TEXTURE_2D, hTexture2);

        int textureUniformHandle2 = glGetUniformLocation(hProgram, "bTexture");
        if (textureUniformHandle2 != -1) glUniform1i(textureUniformHandle2, 1);


        int vboTriangleIndices = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboTriangleIndices);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, triangleIndices, GL_STATIC_DRAW);

        // set up a vao
        int vaoTriangle = glGenVertexArrays();
        glBindVertexArray(vaoTriangle);

        // The position
        int posAttribIndex = glGetAttribLocation(hProgram, "pos");
        if (posAttribIndex != -1) {
            glEnableVertexAttribArray(posAttribIndex);
            glBindBuffer(GL_ARRAY_BUFFER, vboTriangleVertices);
            glVertexAttribPointer(posAttribIndex, 3, GL_FLOAT, false, MAGICNUMBER123, 0);
        }

        // The color
        int colAttribIndex = glGetAttribLocation(hProgram, "col");
        if (colAttribIndex != -1) {
            glEnableVertexAttribArray(colAttribIndex);
            // pointer 12 = 3 pos * 4 (float size)
            glVertexAttribPointer(colAttribIndex, 3, GL_FLOAT, false, MAGICNUMBER123, 12);
        }

        // The texture coordinates
        int textAttribIndex = glGetAttribLocation(hProgram, "tex");
        if (textAttribIndex != -1) {
            glEnableVertexAttribArray(textAttribIndex);
            // pointer 24 = 6 pos * 4 (float size)
            glVertexAttribPointer(textAttribIndex, 2, GL_FLOAT, false, MAGICNUMBER123, 24);
        }

        // The normals
        int norAttribIndex = glGetAttribLocation(hProgram, "norm");
        if (norAttribIndex != -1) {
            glEnableVertexAttribArray(norAttribIndex);
            // pointer 32 = 8 pos * 4 (float size)
            glVertexAttribPointer(norAttribIndex, 3, GL_FLOAT, true, MAGICNUMBER123, 32);
        }

        // The Color of the light
        int lightColAttribIndex = glGetUniformLocation(hProgram, "colLight");
        if (lightColAttribIndex != -1)
            glUniform3f(lightColAttribIndex, 1f, 1f, 1f);

        // 4vec for white
        int whiteColAttributeIndex = glGetUniformLocation(hProgram, "WHITE");
        if (whiteColAttributeIndex != -1)
            glUniform4f(whiteColAttributeIndex, 0f, 0f, 0f,1f);

        // The Color of the light
        int blackColAttributeIndex = glGetUniformLocation(hProgram, "BLACK");
        if (blackColAttributeIndex != -1)
            glUniform4f(blackColAttributeIndex, 1f, 1f, 1f,1f);

        // The Position of the light
        int lightPosAttributeIndex = glGetUniformLocation(hProgram, "posLight");
        if (lightPosAttributeIndex != -1)
            glUniform3f(lightPosAttributeIndex, 0, 0, -10);

        // The Position of the camera
        int camPosAttributeIndex = glGetUniformLocation(hProgram, "posCam");
        if (camPosAttributeIndex != -1)
            glUniform3f(camPosAttributeIndex, cameraPosition.x, cameraPosition.y, cameraPosition.z);

        //shine bright like a
        float diamond = 50f;
        int copyPasteUniformHandle = glGetUniformLocation(hProgram, "diamond");
        if (copyPasteUniformHandle != -1)
            glUniform1f(copyPasteUniformHandle, diamond);


        //sqrt of amount of textls
        int iterations = 4;
        int iterationsUniformHandler = glGetUniformLocation(hProgram, "iterations");
        if (iterationsUniformHandler != -1)
            glUniform1f(iterationsUniformHandler, iterations);

        // reset the buffer
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        // check for errors during all previous calls
        int error = glGetError();
        if (error != GL_NO_ERROR)
            throw new Exception(Integer.toString(error));

        //---------------------------------------------------------------------------------------------------------------

        int texId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texId);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, 512, 512, 0, GL_RGBA, GL_UNSIGNED_BYTE, 0);

        //setup renderbuffer
        int rboId = glGenRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER, rboId);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, 512, 512);

        //setup fbo
        int fboId = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, fboId);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texId, 0);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, rboId);

        assert (glCheckFramebufferStatus(GL_FRAMEBUFFER) == GL_FRAMEBUFFER_COMPLETE);

        // --------------------------------------------------------------------------------------------------------------
        // Scene 1
        // --------------------------------------------------------------------------------------------------------------
        if (SCENE == 1) {
            // ----------------------------------------------------------------------------------------------------------
            // render loop
            long startTime = System.currentTimeMillis();

            while (!GLFW.glfwWindowShouldClose(hWindow)) {
                //render the scene into fbo
                glBindFramebuffer(GL_FRAMEBUFFER, fboId);
                glViewport(0, 0, 512, 512);
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);


                // The Position of the light
                lightPosAttributeIndex = glGetUniformLocation(hProgram, "posLight");
                if (lightPosAttributeIndex != -1)
                    glUniform3f(lightPosAttributeIndex, cameraPosition.x, cameraPosition.y, cameraPosition.z);

                // center cube
                Mat4 mvp = Mat4.multiply(
                        Mat4.perspective(45, 1920f / 1080, 0.1f, 100f), //projection
                        Mat4.lookAt(new Vec3(0, 0, -10), new Vec3(0, 0, 0), new Vec3(0, 1, 0)), //view
                        Mat4.scale(1, 1, 1),
                        Mat4.ID //model
                );

                int mvpLocation = glGetUniformLocation(hProgram, "mvp");
                if (mvpLocation != -1)
                    glUniformMatrix4fv(mvpLocation, false, mvp.toArray());

                glBindVertexArray(vaoTriangle);
                glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboTriangleIndices);
                glDrawElements(GL_TRIANGLES, triangleIndices.length, GL_UNSIGNED_INT, 0);

                // small cube close to center cube
                mvp = Mat4.multiply(
                        Mat4.perspective(45, 1920f / 1080, 0.1f, 100f), //projection
                        Mat4.lookAt(new Vec3(0, 0, -10), new Vec3(0, 0, 0), new Vec3(0, 1, 0)), //view
                        // Math.Mat4.rotate((float) (System.currentTimeMillis() - startTime) * 0.005f, new Math.Vec3(0, 1, 0)), //rotate y
                        // Math.Mat4.rotate((float) (System.currentTimeMillis() - startTime) * 0.2f, new Math.Vec3(1, 0, 0)), //rotate x
                        Mat4.translate(2, 0, 0),
                        Mat4.scale(0.5f, 0.5f, 0.5f),
                        Mat4.ID //model
                );

                mvpLocation = glGetUniformLocation(hProgram, "mvp");
                if (mvpLocation != -1)
                    glUniformMatrix4fv(mvpLocation, false, mvp.toArray());

                glBindVertexArray(vaoTriangle);
                glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboTriangleIndices);
                glDrawElements(GL_TRIANGLES, triangleIndices.length, GL_UNSIGNED_INT, 0);

                // small cube far from center cube
                mvp = Mat4.multiply(
                        Mat4.perspective(45, 1920f / 1080, 0.1f, 100f), //projection
                        Mat4.lookAt(new Vec3(0, 0, -10), new Vec3(0, 0, 0), new Vec3(0, 1, 0)), //view
                        Mat4.rotate(180, new Vec3(0, 1, 0)), //rotate y
                        Mat4.rotate(90, new Vec3(1, 0, 0)), //rotate y
                        Mat4.rotate(-(float) (System.currentTimeMillis() - startTime) * 0.023f, new Vec3(0, 1, 0)), //rotate y
                        Mat4.rotate(-(float) (System.currentTimeMillis() - startTime) * 0.1f, new Vec3(1, 0, 0)), //rotate x
                        Mat4.translate(4, 0, 0),
                        Mat4.scale(0.5f, 0.5f, 0.5f),
                        Mat4.ID //model
                );

                mvpLocation = glGetUniformLocation(hProgram, "mvp");
                if (mvpLocation != -1)
                    glUniformMatrix4fv(mvpLocation, false, mvp.toArray());

                glBindVertexArray(vaoTriangle);
                glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboTriangleIndices);
                glDrawElements(GL_TRIANGLES, triangleIndices.length, GL_UNSIGNED_INT, 0);

                // unbind fbo
                glBindFramebuffer(GL_FRAMEBUFFER, 0);
                glViewport(0, 0, 1920, 1080);
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                // end FBO -------------------------------------------------------------------------------------------------------------------------------------

                //render to screen

                //fbo texture in the center cube
            /*
            glBindTexture(GL_TEXTURE_2D, texId);
            textureUniformHandle = glGetUniformLocation(hProgram, "aTexture");
            if (textureUniformHandle != -1) glUniform1i(textureUniformHandle, 1);
*/

                // The Position of the camera
            /*
            camPosAttributeIndex = glGetUniformLocation(hProgram, "posCam");
            if (camPosAttributeIndex != -1)
                glUniform3f(camPosAttributeIndex, cameraPosition.x, cameraPosition.y, cameraPosition.z);
            */
                // The Position of the light
                lightPosAttributeIndex = glGetUniformLocation(hProgram, "posLight");
                if (lightPosAttributeIndex != -1)
                    glUniform3f(lightPosAttributeIndex, cameraPosition.x, cameraPosition.y, cameraPosition.z);


                glBindTexture(GL_TEXTURE_2D, hTexture3);
                textureUniformHandle = glGetUniformLocation(hProgram, "cTexture");
                if (textureUniformHandle != -1) glUniform1i(textureUniformHandle, 1);
                // center cube

                mvp = Mat4.multiply(
                        Mat4.perspective(45, 1920f / 1080, 0.1f, 100f), //projection
                        Mat4.lookAt(new Vec3(cameraPosition.x, cameraPosition.y, cameraPosition.z), new Vec3(0, 0, 0), new Vec3(0, 1, 0)), //view
                        //Math.Mat4.lookAt(new Math.Vec3(0, 0, -10), new Math.Vec3(0, 0, 0), new Math.Vec3(0, 1, 0)), //view
                        //Math.Mat4.rotate((float) (System.currentTimeMillis() - startTime) * 0.1f, new Math.Vec3(0, 1, 0)), //rotate y
                        Mat4.rotate(-40f, new Vec3(1, 0, 0)),
                        Mat4.translate(0, 40f, 0),
                        Mat4.translate(0,0,-100f),
                        Mat4.translate(boxPosition.x, boxPosition.y, boxPosition.z),
                        Mat4.scale(1000, 5, 1000),
                        //Math.Mat4.scale(3, 3, 3),
                        Mat4.ID //model
                );


                mvpLocation = glGetUniformLocation(hProgram, "mvp");
                if (mvpLocation != -1)
                    glUniformMatrix4fv(mvpLocation, false, mvp.toArray());

                glBindVertexArray(vaoTriangle);
                glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboTriangleIndices);
                glDrawElements(GL_TRIANGLES, triangleIndices.length, GL_UNSIGNED_INT, 0);

                glBindTexture(GL_TEXTURE_2D, hTexture2);
                textureUniformHandle = glGetUniformLocation(hProgram, "bTexture");
                if (textureUniformHandle != -1) glUniform1i(textureUniformHandle, 1);
                // display of the texture

                mvp = Mat4.multiply(
                        Mat4.perspective(45, 1920f / 1080, 0.1f, 100f), //projection
                        Mat4.lookAt(new Vec3(cameraPosition.x, cameraPosition.y, cameraPosition.z), new Vec3(0, 0, 0), new Vec3(0, 1, 0)), //view
                        //Math.Mat4.lookAt(new Math.Vec3(0, 0, -10), new Math.Vec3(0, 0, 0), new Math.Vec3(0, 1, 0)), //view
                        //Math.Mat4.rotate((float) (System.currentTimeMillis() - startTime) * 0.1f, new Math.Vec3(0, 1, 0)), //rotate y
                        // Math.Mat4.rotate(90, new Math.Vec3(1, 0, 0)),
                        Mat4.translate(-11, 3, -125),

                        Mat4.translate(boxPosition2.x, boxPosition2.y, boxPosition2.z),
                        Mat4.scale(5, 5, 0.01f),
                        //Math.Mat4.scale(3, 3, 3),
                        Mat4.ID //model
                );

                mvpLocation = glGetUniformLocation(hProgram, "mvp");
                if (mvpLocation != -1) {
                    glUniformMatrix4fv(mvpLocation, false, mvp.toArray());
                }

                glBindVertexArray(vaoTriangle);
                glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboTriangleIndices);
                glDrawElements(GL_TRIANGLES, triangleIndices.length, GL_UNSIGNED_INT, 0);
            /*
            glBindTexture(GL_TEXTURE_2D, hTexture);
            textureUniformHandle = glGetUniformLocation(hProgram, "aTexture");
            if (textureUniformHandle != -1) glUniform1i(textureUniformHandle, 1);

            // cube close to the center cube
            mvp = Math.Mat4.multiply(
                    Math.Mat4.perspective(45, 720f / 480, 0.1f, 100f), //projection
                    Math.Mat4.lookAt(new Math.Vec3(0, 0, -10), new Math.Vec3(0, 0, 0), new Math.Vec3(0, 1, 0)), //view
                    Math.Mat4.rotate((float) (System.currentTimeMillis() - startTime) * 0.005f, new Math.Vec3(0, 1, 0)), //rotate y
                    Math.Mat4.rotate((float) (System.currentTimeMillis() - startTime) * 0.2f, new Math.Vec3(1, 0, 0)), //rotate x
                    Math.Mat4.translate(2, 0, 0),
                    Math.Mat4.scale(0.5f, 0.5f, 0.5f),
                    Math.Mat4.ID //model
            );

            mvpLocation = glGetUniformLocation(hProgram, "mvp");
            if (mvpLocation != -1)
                glUniformMatrix4fv(mvpLocation, false, mvp.toArray());

            glBindVertexArray(vaoTriangle);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboTriangleIndices);
            glDrawElements(GL_TRIANGLES, triangleIndices.length, GL_UNSIGNED_INT, 0);

            // cube far from the center cube
            mvp = Math.Mat4.multiply(
                    Math.Mat4.perspective(45, 720f / 480, 0.1f, 100f), //projection
                    Math.Mat4.lookAt(new Math.Vec3(0, 0, -10), new Math.Vec3(0, 0, 0), new Math.Vec3(0, 1, 0)), //view
                    Math.Mat4.rotate(180, new Math.Vec3(0, 1, 0)), //rotate y
                    Math.Mat4.rotate(90, new Math.Vec3(1, 0, 0)), //rotate y
                    Math.Mat4.rotate(-(float) (System.currentTimeMillis() - startTime) * 0.023f, new Math.Vec3(0, 1, 0)), //rotate y
                    Math.Mat4.rotate(-(float) (System.currentTimeMillis() - startTime) * 0.1f, new Math.Vec3(1, 0, 0)), //rotate x
                    Math.Mat4.translate(4, 0, 0),
                    Math.Mat4.scale(0.5f, 0.5f, 0.5f),
                    Math.Mat4.ID //model
            );

            glUniformMatrix4fv(mvpLocation, false, mvp.toArray());

            glBindVertexArray(vaoTriangle);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboTriangleIndices);
            glDrawElements(GL_TRIANGLES, triangleIndices.length, GL_UNSIGNED_INT, 0);

             */
                GLFW.glfwSwapBuffers(hWindow);
                GLFW.glfwPollEvents();

                error = glGetError();
                if (error != GL_NO_ERROR)
                    throw new Exception(Integer.toString(error));
            }
        }

        GLFW.glfwDestroyWindow(hWindow);
        GLFW.glfwTerminate();
    }

    //Reads in a picture and returns it as a Buffered Image
    public static BufferedImage getTextureData(String str) throws IOException {
        BufferedImage img;
        img = ImageIO.read(new File(str));
        return img;
    }

    /**
     * @param base         The Base Tiles for the Texture
     * @param amountColors Amount of colours used in the base texture
     * @param row          repetitions in x directions
     * @param col          repetitions in y directions
     * @return
     */
    public static BufferedImage generateCornerTilesTexutre(BufferedImage base, int amountColors, int row, int col) {
        BufferedImage output = new BufferedImage(base.getWidth() * row, base.getHeight() * col, BufferedImage.TYPE_3BYTE_BGR);
        ArrayList<BufferedImage> textls = generateTextls(base, amountColors);
        int tempValue = (int) Math.pow(2, amountColors);
        int possibleValues = (int) Math.pow(tempValue, 2);
        WritableRaster wr = output.getRaster();

        //initialize necessary variables

        int temp, temp2, temp3, temp4, oldValue, oldValue2, oldValueRow;
        int[] lastValues = new int[col * tempValue];
        for (int i = 0; i < row * tempValue; i++) {
            for (int j = 0; j < col * tempValue; j++) {
                if (i == 0) {
                    if (j == 0) {
                        temp = (int) (Math.random() * ((possibleValues - 1) + 1));
                        lastValues[j] = temp;
                        wr.setRect(j * 8, i * 8, textls.get(temp).getRaster());
                    } else {
                        temp = (int) (Math.random() * ((possibleValues - 1) + 1));
                        // we only care about 2 and 3 of previous one and
                        oldValue = lastValues[j - 1];
                        temp2 = temp;
                        temp3 = temp;
                        oldValue2 = oldValue;
                        // Check if the 2 nad 3 bit are equal to the 1 and 4 of the new tile if thats not the case we cant use it

                        while ((((temp2 & 1) ^ ((oldValue >> 1) & 1)) == 1) || ((((temp3 >> 3) & 1) ^ ((oldValue2 >> 1) & 2)) == 1)) {

                            temp = (int) (Math.random() * ((possibleValues - 1) + 1));
                            temp2 = temp;
                            temp3 = temp;
                            oldValue = lastValues[j - 1];
                            oldValue2 = oldValue;
                        }

                        lastValues[j] = temp;
                        wr.setRect(j * 8, i * 8, textls.get(temp).getRaster());
                    }
                } else {
                    if (j == 0) {
                        temp = (int) (Math.random() * ((possibleValues - 1) + 1));
                        oldValue = lastValues[j];
                        temp2 = temp;
                        temp3 = temp;
                        oldValue2 = oldValue;

                        // New 1  == Old 4 && New 2 == Old 3
                        while (((((temp2 >> 3) & 1) ^ (oldValue & 1)) == 1) || ((((temp3 >> 2) & 1) ^ ((oldValue2 >> 1) & 1)) == 1)) {

                            temp = (int) (Math.random() * ((possibleValues - 1) + 1));
                            temp2 = temp;
                            temp3 = temp;
                            oldValue = lastValues[j];
                            oldValue2 = oldValue;
                        }

                        lastValues[j] = temp;
                        wr.setRect(j * 8, i * 8, textls.get(temp).getRaster());

                    } else {
                        // Here We need to combine both the two comparisions we did earlier
                        temp = (int) (Math.random() * ((possibleValues - 1) + 1));
                        oldValue = lastValues[j - 1];
                        oldValue2 = oldValue;
                        temp2 = temp;
                        temp3 = temp;
                        temp4 = temp;
                        oldValueRow = lastValues[j];

                        // New 1 == Old 2 && New 4 == Old 3
                        // In addition we now have to check if the new bit 2 is same as the 3rd bit of the tile in the upper row
                        while ((((temp2 & 1) ^ ((oldValue >> 1) & 1)) == 1) ||
                                ((((temp3 >> 3) & 1) ^ ((oldValue2 >> 2) & 1)) == 1) ||
                                ((((temp4 >> 2) & 1) ^ ((oldValueRow >> 1) & 1)) == 1)) {

                            temp = (int) (Math.random() * ((possibleValues - 1) + 1));
                            temp2 = temp;
                            temp3 = temp;
                            temp4 = temp;
                            oldValue = lastValues[j-1];
                            oldValue2 = oldValue;
                            oldValueRow = lastValues[j];
                        }

                        lastValues[j] = temp;
                        wr.setRect(j * 8, i * 8, textls.get(temp).getRaster());
                    }
                }
            }
        }
        output.setData(wr);

        return output;


    }

    // Giga scuffed needs to be changed if possible
    public static ArrayList<BufferedImage> generateTextls(BufferedImage base, int amountColors) {
        ArrayList output = new ArrayList();
        if (amountColors == 2) {
            BufferedImage zero = base.getSubimage(0, 24, 8, 8);
            output.add(zero);
            BufferedImage one = base.getSubimage(0, 0, 8, 8);
            output.add(one);
            BufferedImage two = base.getSubimage(8, 24, 8, 8);
            output.add(two);
            BufferedImage three = base.getSubimage(24, 0, 8, 8);
            output.add(three);
            BufferedImage four = base.getSubimage(0, 16, 8, 8);
            output.add(four);
            BufferedImage five = base.getSubimage(16, 24, 8, 8);
            output.add(five);
            BufferedImage six = base.getSubimage(8, 0, 8, 8);
            output.add(six);
            BufferedImage seven = base.getSubimage(8, 8, 8, 8);
            output.add(seven);
            BufferedImage eight = base.getSubimage(24, 24, 8, 8);
            output.add(eight);
            BufferedImage nine = base.getSubimage(24, 16, 8, 8);
            output.add(nine);
            BufferedImage zero1 = base.getSubimage(0, 8, 8, 8);
            output.add(zero1);
            BufferedImage one1 = base.getSubimage(16, 0, 8, 8);
            output.add(one1);
            BufferedImage two1 = base.getSubimage(8, 16, 8, 8);
            output.add(two1);
            BufferedImage three1 = base.getSubimage(24, 8, 8, 8);
            output.add(three1);
            BufferedImage four1 = base.getSubimage(16, 16, 8, 8);
            output.add(four1);
            BufferedImage five1 = base.getSubimage(16, 8, 8, 8);
            output.add(five1);


        }
        return output;
    }
}

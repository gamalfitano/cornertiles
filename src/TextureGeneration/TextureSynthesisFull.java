package TextureGeneration;

import org.jgrapht.Graph;
import org.jgrapht.alg.flow.PushRelabelMFImpl;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;

public class TextureSynthesisFull {
    // Amount of different tests it performs
    static int ITERATIONS = 10;
    static boolean DISPLAYMINCUT = true;
    static int scalingValue = 1;
    static int sizeOfSquare = scalingValue*256;
    static String inputFilePath = "Resource/preTextureSynthesis/seamless2.jpg";
    static String outputFilePath = "Resource/postTextureSynthesis/Bricks123Test.jpg";
    static String templateFilePath = "Resource/Template/plus.tif";

    static int height = 0;
    static int width =0;
    public static void main(String[] args) throws IOException {

        // Input File
        BufferedImage input;
        input = ImageIO.read(new File(inputFilePath));

        // Create an output File where the texture should be saved into
        BufferedImage output = new BufferedImage(1024, 1024, BufferedImage.TYPE_3BYTE_BGR);
        height = input.getHeight();
        width = input.getWidth();
        System.out.println(height + " " + width);
        WritableRaster wrInput = input.getRaster();
        WritableRaster wrOutput = output.getRaster();



        // the size ofeach textel is 256 * 256 this means that each corner has a max size of 64 * 64, we want to test which number is the best for the site
        // startig with 50
        // after some testing we need to use a bigger image or it will look very low res and then scale it down
        // Keep it a multiple of 5

        // Choose n Amount of random squres in the texture to represent the corner colors (n being the amount of colors)

        Random random = new Random();

        int yellowX = random.nextInt(width - sizeOfSquare + 1);
        int yellowY = random.nextInt(height - sizeOfSquare + 1);

        int redX = random.nextInt(width - sizeOfSquare + 1);
        int redY = random.nextInt(height - sizeOfSquare + 1);


        System.out.println("Preprocessing necessary Writable Rasters");
        // We need to make 4 one for every corner
        WritableRaster redCorner = wrInput.createWritableChild(redX, redY, sizeOfSquare, sizeOfSquare, 0, 0, null);
        WritableRaster yellowCorner = wrInput.createWritableChild(yellowX, yellowY, sizeOfSquare, sizeOfSquare, 0, 0, null);

        // save them in a seperate image to test the output

        BufferedImage redCornerImage = new BufferedImage(sizeOfSquare, sizeOfSquare, BufferedImage.TYPE_3BYTE_BGR);
        BufferedImage yellowCornerImage = new BufferedImage(sizeOfSquare, sizeOfSquare, BufferedImage.TYPE_3BYTE_BGR);
        BufferedImage redCornerImageScaled = new BufferedImage(sizeOfSquare / scalingValue, sizeOfSquare / scalingValue, BufferedImage.TYPE_3BYTE_BGR);
        BufferedImage yellowCornerImageScaled = new BufferedImage(sizeOfSquare / scalingValue, sizeOfSquare / scalingValue, BufferedImage.TYPE_3BYTE_BGR);

        redCornerImage.setData(redCorner);
        yellowCornerImage.setData(yellowCorner);


        // Scale down by 5
        AffineTransform af = AffineTransform.getScaleInstance(1.0 / scalingValue, 1.0 / scalingValue);
        AffineTransformOp scaling = new AffineTransformOp(af, AffineTransformOp.TYPE_BICUBIC);
        redCornerImageScaled = scaling.filter(redCornerImage, redCornerImageScaled);
        yellowCornerImageScaled = scaling.filter(yellowCornerImage, yellowCornerImageScaled);

        // Save the images for testing purposes
        File redCornerFile = new File("Resource/redCorner.jpg");
        ImageIO.write(redCornerImageScaled, "jpg", redCornerFile);
        File yellowCornerFile = new File("Resource/yellowCorner.jpg");
        ImageIO.write(yellowCornerImageScaled, "jpg", yellowCornerFile);

        // Load in the template for the mincut

        // since the base template image is a tif well use 1 2 3 but not the 4th number
        BufferedImage template = ImageIO.read(new File(templateFilePath));
        WritableRaster templateRaster = template.getRaster();


        // next step is to create the single textels and save the corner values in the corners
        // size of the textl is 256 * 256 so there is a cross of the size 56 * 56 in the middle which has to be filled up via
        // texture synthesis


        WritableRaster redScaled = redCornerImageScaled.getRaster();
        WritableRaster yellowScaled = yellowCornerImageScaled.getRaster();


        ArrayList<BufferedImage> texture = new ArrayList<>();

        // iterate over every one of the possible 16 textls
        for (int i = 0; i < 16; i++) {
            int counterTemp = 0;
            System.out.println("Textl : " + (i+1));
            BufferedImage textl = new BufferedImage(256, 256, BufferedImage.TYPE_3BYTE_BGR);
            WritableRaster textlRaster = textl.getRaster();

            // First we build the corners of the WritableRaster
            //top left
            if ((i / 8) % 2 == 1) {
                textlRaster.setRect(0, 0, getSpecificCorner(yellowScaled, 2));
                counterTemp += 8;
            } else {
                textlRaster.setRect(0, 0, getSpecificCorner(redScaled, 2));
            }
            //top right
            if ((i / 4) % 2 == 1) {
                textlRaster.setRect(128, 0, getSpecificCorner(yellowScaled, 3));
                counterTemp+=4;
            } else {
                textlRaster.setRect(128, 0, getSpecificCorner(redScaled, 3));
            }

            //bottom right
            if ((i / 2) % 2 == 1) {
                textlRaster.setRect(128, 128, getSpecificCorner(yellowScaled, 0));
                counterTemp +=2;
            } else {
                textlRaster.setRect(128, 128, getSpecificCorner(redScaled, 0));
            }

            //bottom left
            if (i % 2 == 1) {
                textlRaster.setRect(0, 128, getSpecificCorner(yellowScaled, 1));
                counterTemp +=1;
            } else {
                textlRaster.setRect(0, 128, getSpecificCorner(redScaled, 1));
            }

            System.out.println("Counter : " + counterTemp);
            textlRaster = finalizeTextl(textlRaster, wrInput, scalingValue, templateRaster);
            textl.setData(textlRaster);

            texture.add(textl);


        }


        File temp = new File("Resource/PresentationFiles/BricksMinCut.jpg");
        ImageIO.write(texture.get(0), "jpg", temp);
        File temp2 = new File("Resource/PresentationFiles/BricksMinCut2.jpg");
        ImageIO.write(texture.get(2), "jpg", temp2);


        // Size of one textel --> 256 X 256
        // Texture size = 1024 X 1024
        // For 2 colors create

        // now we have to set the output of the output writable raster to the other textls

        wrOutput.setRect(0,768,texture.get(0).getRaster());
        wrOutput.setRect(0,0,texture.get(1).getRaster());
        wrOutput.setRect(256,768,texture.get(2).getRaster());
        wrOutput.setRect(768,0,texture.get(3).getRaster());
        wrOutput.setRect(0,512,texture.get(4).getRaster());
        wrOutput.setRect(512,768,texture.get(5).getRaster());
        wrOutput.setRect(256,0,texture.get(6).getRaster());
        wrOutput.setRect(256,256,texture.get(7).getRaster());
        wrOutput.setRect(768,768,texture.get(8).getRaster());
        wrOutput.setRect(768,512,texture.get(9).getRaster());
        wrOutput.setRect(0,256,texture.get(10).getRaster());
        wrOutput.setRect(512,0,texture.get(11).getRaster());
        wrOutput.setRect(256,512,texture.get(12).getRaster());
        wrOutput.setRect(768,256,texture.get(13).getRaster());
        wrOutput.setRect(512,512,texture.get(14).getRaster());
        wrOutput.setRect(512,256,texture.get(15).getRaster());

        output.setData(wrOutput);
        File outputFile = new File(outputFilePath);
        ImageIO.write(output, "jpg", outputFile);

    }

    private static WritableRaster finalizeTextl(WritableRaster textl, WritableRaster wrInput, int scalingValue, WritableRaster template) {

        double currentMaxFlow = Double.MAX_VALUE;

        SimpleWeightedGraph<Integer, DefaultWeightedEdge> graph =
                new SimpleWeightedGraph<Integer, DefaultWeightedEdge>(DefaultWeightedEdge.class);
        // Add all the Vertices
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
                graph.addVertex(i * 256 + j);
            }
        }

        for (int k = 0; k < ITERATIONS; k++) {


            BufferedImage testingImage = getDiamond(wrInput, scalingValue);
            WritableRaster diamond = testingImage.getRaster();

            createEdges(graph);
            calculateLoss(textl, diamond, graph, template);

            PushRelabelMFImpl minCut = new PushRelabelMFImpl(graph);
            double temp = minCut.calculateMaximumFlow(65536, 65537);

            if (temp > currentMaxFlow) continue;

            currentMaxFlow = temp;

            System.out.println("Current max flow : " + temp);
            minCut.getMaximumFlow(65536, 65537);
            Set sink = minCut.getSinkPartition();
            Set source = minCut.getSourcePartition();
            int[] WHITE = new int[]{255, 255, 255};
            int[] pixels = new int[3];
            for (int i = 0; i < 256; i++) {
                for (int j = 0; j < 256; j++) {
                    if (sink.contains(i * 256 + j)) {
                        if (DISPLAYMINCUT){
                            textl.setPixel(j,i,WHITE);
                        }
                        else {
                            diamond.getPixel(j, i, pixels);
                            textl.setPixel(j, i, pixels);

                        }
                    }
                }
            }
        }
        return textl;
    }

    /***
     * Create edges for the ArrayList
     * @param graph
     */
    private static void createEdges(Graph<Integer, DefaultWeightedEdge> graph) {

        for (int i = 0; i < 256 * 256; i++) {
            int y = i / 256;
            int x = i % 256;

            //if (!(((Math.abs(x - 63) + Math.abs(y - 63)) <= 64)) && !((Math.abs(x - 63) + Math.abs(y - 63)) > 50)) continue;
            // the edge to the right
            if (x != 255) {
                graph.addEdge(i, i + 1);
                graph.setEdgeWeight(graph.getEdge(i, i + 1), 100000);
            }
            // the edge down
            if (y != 255) {
                graph.addEdge(i, i + 256);
                graph.setEdgeWeight(graph.getEdge(i, i + 256), 100000);
            }
        }

    }

    private static int[][] findPathTopLeft(int[][][] costFuntion) {

        // this array is build that it displays how much space there is till the cut off on each side
        int[][] output = new int[256][2];

        for (int i = 0; i < output.length; i++) {
            output[i][0] = 255;
            output[i][1] = 255;
        }
        // Start 127 / 2
        int x = 2;
        int y = 127;

        // Top Left
        while (x < 128) {
            // System.out.println("TOP : " + costFuntion[y][x][0] + "  RIGHT : " + costFuntion[y][x][1]);
            if (costFuntion[y][x][0] < costFuntion[y][x][1]) {

                output[y][0] = x;
                y--;
            } else {
                x++;
            }

        }
        // now x will be 127 and y somewhere in the band which is allowed ergo somewhere around 1 and 40ish
        // Top Right
        while (y < 128) {
            if (costFuntion[x][y][1] >= costFuntion[x][y][2]) {
                // now we are on the right side of the diamond shaped texture so we fill in the 2nd side
                output[y][1] = x;
                y++;
            } else {
                x++;
            }

        }

        // y should be 127 and x between 210 and 255 --> x has to be smaller than 128
        while (x >= 128) {
            if (costFuntion[x][y][2] < costFuntion[x][y][3]) {
                // now we are on the right side of the diamond shaped texture so we fill in the 2nd side
                output[y][1] = x;
                y++;
            } else {
                x--;
            }

        }
        //y = high value x = 127
        while (y >= 128) {
            if (costFuntion[x][y][3] >= costFuntion[x][y][0]) {
                // System.out.println("CUURENT : " + y + " | " + x );
                // now we are on the right side of the diamond shaped texture so we fill in the 2nd side
                output[y][0] = x;
                y--;
            } else {
                x--;
            }

        }

        return output;
    }

    private static void calculateLoss(WritableRaster main, WritableRaster diamond, SimpleWeightedGraph<Integer, DefaultWeightedEdge> graph, WritableRaster template) {
        // we set everything which is more than 126 away from the center to 1000 since we dont want to use them
        // we set the cost function to the difference between the current pixel and all the adjacent ones
        // one note

        // Source Vertex
        graph.addVertex(65536);
        // Sink Vertex
        graph.addVertex(65537);

        int[] pixel1 = new int[3];
        int[] pixel2 = new int[3];
        int[] pixel3 = new int[3];
        int[] pixel4 = new int[3];
        int[] pixel5 = new int[3];
        int[] pixel6 = new int[3];
        int[] templ = new int[4];
        for (int i = 0; i < 256; i++) {


            for (int j = 0; j < 256; j++) {

                // if the pixel is Black it means --> connect to source
                // If its red it means --> connect to sink
                // else means its in the middle and should be decided by min cut

                // red is 237 28 36 since i wasnt paying attention when creating the templates
                template.getPixel(j, i, templ);
                // BLACK --> outside
                if (templ[0] == 0 && templ[1] == 0 && templ[2] == 0) {
                    graph.addEdge(i * 256 + j, 65536);
                    graph.setEdgeWeight(i * 256 + j, 65536, Double.MAX_VALUE);
                }
                // WHITE --> inside
                else if (templ[0] == 237 && templ[1] == 28 && templ[2] == 36) {
                    graph.addEdge(i * 256 + j, 65537);
                    graph.setEdgeWeight(i * 256 + j, 65537, Double.MAX_VALUE);
                }

                // we are in the intresting middle area
                else {
                    // we take the additional pixels in direction middle and not in direction bottom left so that we wont have any problems with edge cases
                    double weightHorizontal = 0, weightVertical = 0;
                    if (i < 128) {
                        main.getPixel(j - 1, i, pixel1);
                        main.getPixel(j, i, pixel2);
                        main.getPixel(j + 1, i, pixel3);
                        diamond.getPixel(j - 1, i, pixel4);
                        diamond.getPixel(j, i, pixel5);
                        diamond.getPixel(j + 1, i, pixel6);
                        weightVertical = calcWeight(pixel1, pixel2, pixel3, pixel4, pixel5, pixel6);
                        graph.setEdgeWeight(graph.getEdge((i - 1) * 256 + j, i * 256 + j), weightVertical);
                    } else {
                        main.getPixel(j + 1, i, pixel1);
                        main.getPixel(j, i, pixel2);
                        main.getPixel(j - 1, i, pixel3);
                        diamond.getPixel(j + 1, i, pixel4);
                        diamond.getPixel(j, i, pixel6);
                        diamond.getPixel(j - 1, i, pixel5);
                        weightVertical = calcWeight(pixel1, pixel2, pixel3, pixel4, pixel5, pixel6);
                        graph.setEdgeWeight(graph.getEdge((i + 1) * 256 + j, i * 256 + j), weightVertical);
                    }

                    if (j < 128) {
                        main.getPixel(j, i - 1, pixel1);
                        main.getPixel(j, i, pixel3);
                        main.getPixel(j, i + 1, pixel2);
                        diamond.getPixel(j, i - 1, pixel4);
                        diamond.getPixel(j, i, pixel6);
                        diamond.getPixel(j, i + 1, pixel5);
                        weightHorizontal = calcWeight(pixel1, pixel2, pixel3, pixel4, pixel5, pixel6);
                        graph.setEdgeWeight(graph.getEdge(i * 256 + j - 1, i * 256 + j), weightHorizontal);
                    } else {
                        main.getPixel(j, i + 1, pixel1);
                        main.getPixel(j, i, pixel3);
                        main.getPixel(j, i - 1, pixel2);
                        diamond.getPixel(j, i + 1, pixel4);
                        diamond.getPixel(j, i, pixel6);
                        diamond.getPixel(j, i - 1, pixel5);
                        weightHorizontal = calcWeight(pixel1, pixel2, pixel3, pixel4, pixel5, pixel6);
                        graph.setEdgeWeight(graph.getEdge(i * 256 + j + 1, i * 256 + j), weightHorizontal);
                    }

                }
            }
        }
    }


    // Calcs the weight between 2 pixels --> I being the pixels from the base image and O being the pixels from the diamondform

    public static double calcWeight(int[] I1, int[] I2, int[] I3, int[] O1, int[] O2, int[] O3) {
        int temp[] = new int[3];
        for (int i = 0; i < temp.length; i++) {
            temp[i] = O1[i] - I1[i];
        }
        double top1 = Math.sqrt((temp[0] * temp[0]) + (temp[1] * temp[1]) + (temp[2] * temp[2]));

        for (int i = 0; i < temp.length; i++) {
            temp[i] = O2[i] - I2[i];
        }
        double top2 = Math.sqrt((temp[0] * temp[0]) + (temp[1] * temp[1]) + (temp[2] * temp[2]));

        // calculate the gradient in direction of the 2nd pixel ergo. I1 --> I2 --> I3 and O1 --> O2 --> O3
        int[] diff = new int[3];
        double[] t = new double[3];

        for (int i = 0; i < diff.length; i++) {
            diff[i] = I2[i] - I1[i];
        }
        for (int i = 0; i < t.length; i++) {
            t[i] = (double) I1[i] / diff[i];
        }
        Double bottom1 = Math.sqrt(Math.pow(t[0], 2) + Math.pow(t[1], 2) + Math.pow(t[2], 2));

        for (int i = 0; i < diff.length; i++) {
            diff[i] = I3[i] - I2[i];
        }
        for (int i = 0; i < t.length; i++) {
            t[i] = (double) I2[i] / diff[i];
        }
        Double bottom2 = Math.sqrt(Math.pow(t[0], 2) + Math.pow(t[1], 2) + Math.pow(t[2], 2));

        for (int i = 0; i < diff.length; i++) {
            diff[i] = O2[i] - O1[i];
        }
        for (int i = 0; i < t.length; i++) {
            t[i] = (double) O1[i] / diff[i];
        }
        Double bottom3 = Math.sqrt(Math.pow(t[0], 2) + Math.pow(t[1], 2) + Math.pow(t[2], 2));

        for (int i = 0; i < diff.length; i++) {
            diff[i] = O3[i] - O2[i];
        }
        for (int i = 0; i < t.length; i++) {
            t[i] = (double) O2[i] / diff[i];
        }


        Double bottom4 = Math.sqrt(Math.pow(t[0], 2) + Math.pow(t[1], 2) + Math.pow(t[2], 2));

        double bottom = 0;

        if (!bottom1.isNaN() && !bottom1.isInfinite()) bottom += bottom1;
        if (!bottom2.isNaN() && !bottom2.isInfinite()) bottom += bottom2;
        if (!bottom3.isNaN() && !bottom3.isInfinite()) bottom += bottom3;
        if (!bottom4.isNaN() && !bottom4.isInfinite()) bottom += bottom4;

        if (bottom == 0 || bottom == Double.POSITIVE_INFINITY) {
            bottom = 60;
        }

        //System.out.println(bottom);
        double output = (top1 + top2) / bottom;


        //System.out.println(output);
        return output;
    }


    public int[] createTextl() {
        int[] output = new int[256 * 256 * 3];
        return output;
    }

    /**
     * @param main   Writable Raster which is the red or yellow square --> should be size 256*256 here
     *               TODO make size dynamic
     * @param corner The number of the corner starting top left and going clockwise
     * @return
     */
    public static WritableRaster getSpecificCorner(WritableRaster main, int corner) {
        WritableRaster output = null;
        switch (corner) {
            case 0:
                output = main.createWritableChild(0, 0, 128, 128, 0, 0, null);
                break;
            case 1:
                output = main.createWritableChild(128, 0, 128, 128, 0, 0, null);
                break;
            case 2:
                output = main.createWritableChild(128, 128, 128, 128, 0, 0, null);
                break;
            case 3:
                output = main.createWritableChild(0, 128, 128, 128, 0, 0, null);
                break;
            default:
                System.out.println("We should never get to this --> Check getSpecific Corner Implementation");
        }
        return output;

    }

    public static BufferedImage getDiamond(WritableRaster main, int scaleFactor) {
        // randomize --> Actually get the whole square not only the diamond

        Random random = new Random();

        int startX = random.nextInt(width - sizeOfSquare + 1);
        int startY = random.nextInt(height - sizeOfSquare + 1);


        BufferedImage out = new BufferedImage(256 * scaleFactor, 256 * scaleFactor, BufferedImage.TYPE_3BYTE_BGR);
        ;
        BufferedImage output = new BufferedImage(256, 256, BufferedImage.TYPE_3BYTE_BGR);
        ;
        WritableRaster temp = main.createWritableChild(startX, startY, 256 * scaleFactor, 256 * scaleFactor, 0, 0, null);
        out.setData(temp);


        AffineTransform af = AffineTransform.getScaleInstance(1.0 / scaleFactor, 1.0 / scaleFactor);
        AffineTransformOp scaling = new AffineTransformOp(af, AffineTransformOp.TYPE_BICUBIC);


        output = scaling.filter(out, output);
        // now we have to create a diamond form
        WritableRaster wr = output.getRaster();
        //int benchmark = 127;
        //int[] WHITE = new int[]{255, 255, 255};
        //for (int i = 0; i < output.getHeight(); i++) {
        //    for (int j = 0; j < output.getWidth(); j++) {
        // We keep the Values if they are inside this diamond shaped form otherwise we set the pixel to 0
        // First Test is by removing everything which is too far from the middle point
        //if ((Math.abs(i - 127) + Math.abs(j - 127)) > benchmark) {
        //wr.setPixel(i, j, WHITE);
        //}
        //    }
        //}
        output.setData(wr);
        //System.out.println(" TEST " + output.getHeight() + " " + output.getWidth());
        return output;
    }


}



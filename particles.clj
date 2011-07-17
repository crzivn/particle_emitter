(ns particles
  (:use particle emitter utils)
  (:import (java.io File)
           (java.awt Dimension)
           (java.awt.event MouseAdapter MouseEvent)
           (javax.swing JFrame)
           (javax.media.opengl GL2 GLEventListener GLAutoDrawable GLProfile GLCapabilities)
           (javax.media.opengl.awt GLCanvas)
           (com.sun.opengl.util Animator)
           (com.sun.opengl.util.texture Texture TextureIO)))


(def canvas (GLCanvas. (GLCapabilities. (GLProfile/get (GLProfile/GL2GL3)))))
(def animator (Animator. canvas))

(def squareList (ref 0))
(def particleImg (ref nil))
(def emitterImg (ref nil))

(def particles (ref []))
;(def add-particles (ref false))


(defn initSquareDisplayList [gl]
  (dosync (ref-set squareList (.glGenLists gl 1)))
  (doto gl
    (.glNewList @squareList GL2/GL_COMPILE)
    (.glBegin GL2/GL_POLYGON)
    (.glTexCoord2f 0 0) (.glVertex2f -0.5 -0.5)
    (.glTexCoord2f 1 0) (.glVertex2f  0.5 -0.5)
    (.glTexCoord2f 1 1) (.glVertex2f  0.5  0.5)
    (.glTexCoord2f 0 1) (.glVertex2f -0.5  0.5)
    (.glEnd)
    (.glEndList)))


(defn renderImage [gl texture location diameter color]
  (doto gl
    (.glBindTexture GL2/GL_TEXTURE_2D (.getTextureObject texture))

    (.glPushMatrix)
    (.glTranslatef (:x location) (:y location) (:z location))
    (.glScalef diameter diameter diameter)
    (.glColor3fv (float-array color) 0)
    (.glCallList @squareList)
    (.glPopMatrix)))


(defn loadTexture [filename]
  (let [texture (TextureIO/newTexture (File. filename) false)]

    (.setTexParameteri texture GL2/GL_TEXTURE_WRAP_S GL2/GL_REPEAT)
    (.setTexParameteri texture GL2/GL_TEXTURE_WRAP_T GL2/GL_REPEAT)
    (.setTexParameteri texture GL2/GL_TEXTURE_MAG_FILTER GL2/GL_NEAREST)
    (.setTexParameteri texture GL2/GL_TEXTURE_MAG_FILTER GL2/GL_NEAREST)

    texture))


(def canvasEventHandler (proxy [GLEventListener] []
  (init [#^GLAutoDrawable drawable]
        (let [gl (.. drawable getGL getGL2)]
          (doto gl
            (.glShadeModel GL2/GL_SMOOTH)
            (.glEnable GL2/GL_DEPTH_TEST)
            (.glEnable GL2/GL_POINT_SMOOTH)
            (.glHint GL2/GL_PERSPECTIVE_CORRECTION_HINT GL2/GL_NICEST)
            (.setSwapInterval 1))   ; swap buffers on vertical refresh

          (dosync
            (ref-set particleImg (loadTexture "data/particle.png"))
            (ref-set emitterImg (loadTexture "data/emitter.png")))

          (initSquareDisplayList gl)))

  (display [#^GLAutoDrawable drawable]
           (let [gl (.. drawable getGL getGL2)]
             (doto gl
               (.glClear (bit-or GL2/GL_COLOR_BUFFER_BIT
                                 GL2/GL_DEPTH_BUFFER_BIT))
               (.glMatrixMode GL2/GL_MODELVIEW)
               (.glLoadIdentity)

               ; turn on additive blending
               (.glDepthMask false)
               (.glEnable GL2/GL_BLEND)
               (.glBlendFunc GL2/GL_SRC_ALPHA GL2/GL_ONE))

             ; render textures
             (.glEnable gl GL2/GL_TEXTURE_2D)
             (renderImage gl @emitterImg {:x 0 :y 0 :z 0.0} 0.5 [1.0 1.0 1.0 1.0])
             (renderImage gl @particleImg {:x 0.2 :y 0.1 :z 0.0} 0.5 [1.0 1.0 1.0 1.0])
             (.glDisable gl GL2/GL_TEXTURE_2D)

             (.glFlush gl)))

  (reshape [#^GLAutoDrawable drawable x y w h]
           (let [gl (.. drawable getGL getGL2)
                 aspect (/ (double w) (double h))]))
             
  (displayChanged [#^GLAutoDrawable drawable m h])))


(def mouseHandler (proxy [MouseAdapter] []
  ;(mousePressed [#^MouseEvent event]
  ;              (dosync (ref-set particles (add-particles 5 particles))) (println @particles))

  ;(mouseReleased [#^MouseEvent event]
  ;               (dosync (ref-set add-particles false)))))
  ))


(defn main []
  (let [frame (JFrame. "Particle Emitter")]
    (doto canvas
      (.addMouseListener mouseHandler)
      ;(.addMouseMotionListener mouseMotionHandler)
      ;(.addMouseWheelListener mouseWheelHandler)
      (.addGLEventListener canvasEventHandler)
      (.setPreferredSize (Dimension. 640 480)))

    (doto frame
      (.add canvas)
      (.setSize 640 480)
      (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
      (.pack)
      (.setVisible true))

    (.requestFocus canvas)))

(main)
(.start animator)

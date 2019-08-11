package zelgius.com.barcodereader

/**
 * Created by Ezequiel Adrian on 26/02/2017.
 */

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.PorterDuff

import com.google.android.gms.vision.face.Face
import com.google.android.gms.vision.face.Landmark

/**
 * Graphic instance for rendering face position, orientation, and landmarks within an associated
 * graphic overlay view.
 */
class FaceGraphic(overlay: GraphicOverlay<*>, context: Context) : GraphicOverlay.Graphic(overlay) {
    private val marker: Bitmap

    private val opt: BitmapFactory.Options = BitmapFactory.Options()
    private val resources: Resources

    private var faceId: Int = 0
    private lateinit var facePosition: PointF
    private var faceWidth: Float = 0.toFloat()
    private var faceHeight: Float = 0.toFloat()
    private var faceCenter: PointF? = null
    private var smilingProbability = -1f
    private var eyeRightOpenProbability = -1f
    private var eyeLeftOpenProbability = -1f
    private var eulerZ: Float = 0.toFloat()
    private var eulerY: Float = 0.toFloat()
    private var leftEyePos: PointF? = null
    private var rightEyePos: PointF? = null
    private var noseBasePos: PointF? = null
    private var leftMouthCorner: PointF? = null
    private var rightMouthCorner: PointF? = null
    private var mouthBase: PointF? = null
    private var leftEar: PointF? = null
    private var rightEar: PointF? = null
    private var leftEarTip: PointF? = null
    private var rightEarTip: PointF? = null
    private var leftCheek: PointF? = null
    private var rightCheek: PointF? = null

    @Volatile
    private var mFace: Face? = null

    init {
        opt.inScaled = false
        resources = context.resources
        marker = BitmapFactory.decodeResource(resources, R.drawable.marker, opt)
    }

    fun setId(id: Int) {
        faceId = id
    }

    /**
     * Updates the face instance from the detection of the most recent frame.  Invalidates the
     * relevant portions of the overlay to trigger a redraw.
     */
    fun updateFace(face: Face) {
        mFace = face
        postInvalidate()
    }

    fun goneFace() {
        mFace = null
    }

    override fun draw(canvas: Canvas) {
        val face = mFace
        if (face == null) {
            canvas.drawColor(0, PorterDuff.Mode.CLEAR)
            smilingProbability = -1f
            eyeRightOpenProbability = -1f
            eyeLeftOpenProbability = -1f
            return
        }

        facePosition = PointF(translateX(face.position.x), translateY(face.position.y))
        faceWidth = face.width * 4
        faceHeight = face.height * 4
        faceCenter = PointF(
            translateX(face.position.x + faceWidth / 8),
            translateY(face.position.y + faceHeight / 8)
        )
        smilingProbability = face.isSmilingProbability
        eyeRightOpenProbability = face.isRightEyeOpenProbability
        eyeLeftOpenProbability = face.isLeftEyeOpenProbability
        eulerY = face.eulerY
        eulerZ = face.eulerZ
        //DO NOT SET TO NULL THE NON EXISTENT LANDMARKS. USE OLDER ONES INSTEAD.
        for (landmark in face.landmarks) {
            when (landmark.type) {
                Landmark.LEFT_EYE -> leftEyePos =
                    PointF(translateX(landmark.position.x), translateY(landmark.position.y))
                Landmark.RIGHT_EYE -> rightEyePos =
                    PointF(translateX(landmark.position.x), translateY(landmark.position.y))
                Landmark.NOSE_BASE -> noseBasePos =
                    PointF(translateX(landmark.position.x), translateY(landmark.position.y))
                Landmark.LEFT_MOUTH -> leftMouthCorner =
                    PointF(translateX(landmark.position.x), translateY(landmark.position.y))
                Landmark.RIGHT_MOUTH -> rightMouthCorner =
                    PointF(translateX(landmark.position.x), translateY(landmark.position.y))
                Landmark.BOTTOM_MOUTH -> mouthBase =
                    PointF(translateX(landmark.position.x), translateY(landmark.position.y))
                Landmark.LEFT_EAR -> leftEar =
                    PointF(translateX(landmark.position.x), translateY(landmark.position.y))
                Landmark.RIGHT_EAR -> rightEar =
                    PointF(translateX(landmark.position.x), translateY(landmark.position.y))
                Landmark.LEFT_EAR_TIP -> leftEarTip =
                    PointF(translateX(landmark.position.x), translateY(landmark.position.y))
                Landmark.RIGHT_EAR_TIP -> rightEarTip =
                    PointF(translateX(landmark.position.x), translateY(landmark.position.y))
                Landmark.LEFT_CHEEK -> leftCheek =
                    PointF(translateX(landmark.position.x), translateY(landmark.position.y))
                Landmark.RIGHT_CHEEK -> rightCheek =
                    PointF(translateX(landmark.position.x), translateY(landmark.position.y))
            }
        }

        val mPaint = Paint()
        mPaint.color = Color.WHITE
        mPaint.strokeWidth = 4f
        if (faceCenter != null)
            canvas.drawBitmap(marker, faceCenter!!.x, faceCenter!!.y, null)
        if (noseBasePos != null)
            canvas.drawBitmap(marker, noseBasePos!!.x, noseBasePos!!.y, null)
        if (leftEyePos != null)
            canvas.drawBitmap(marker, leftEyePos!!.x, leftEyePos!!.y, null)
        if (rightEyePos != null)
            canvas.drawBitmap(marker, rightEyePos!!.x, rightEyePos!!.y, null)
        if (mouthBase != null)
            canvas.drawBitmap(marker, mouthBase!!.x, mouthBase!!.y, null)
        if (leftMouthCorner != null)
            canvas.drawBitmap(marker, leftMouthCorner!!.x, leftMouthCorner!!.y, null)
        if (rightMouthCorner != null)
            canvas.drawBitmap(marker, rightMouthCorner!!.x, rightMouthCorner!!.y, null)
        if (leftEar != null)
            canvas.drawBitmap(marker, leftEar!!.x, leftEar!!.y, null)
        if (rightEar != null)
            canvas.drawBitmap(marker, rightEar!!.x, rightEar!!.y, null)
        if (leftEarTip != null)
            canvas.drawBitmap(marker, leftEarTip!!.x, leftEarTip!!.y, null)
        if (rightEarTip != null)
            canvas.drawBitmap(marker, rightEarTip!!.x, rightEarTip!!.y, null)
        if (leftCheek != null)
            canvas.drawBitmap(marker, leftCheek!!.x, leftCheek!!.y, null)
        if (rightCheek != null)
            canvas.drawBitmap(marker, rightCheek!!.x, rightCheek!!.y, null)
    }
}
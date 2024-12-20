package com.zelgius.myrecipes.ia.model.stableHorde.request
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GenerationInputStable(
    @SerialName("prompt")
    val prompt: String, // The prompt which will be sent to Stable Diffusion to generate an image

    @SerialName("params")
    val params: ModelGenerationInputStable?, // Nested object for model parameters

    @SerialName("nsfw")
    val nsfw: Boolean = false, // Set to true for NSFW requests

    @SerialName("trusted_workers")
    val trustedWorkers: Boolean = false, // Only trusted workers will serve the request

    @SerialName("validated_backends")
    val validatedBackends: Boolean = true, // Only validated backends will serve the request

    @SerialName("slow_workers")
    val slowWorkers: Boolean = true, // Allows slower workers to pick up the request

    @SerialName("extra_slow_workers")
    val extraSlowWorkers: Boolean = false, // Allows very slow workers to pick up the request

    @SerialName("censor_nsfw")
    val censorNsfw: Boolean = false, // If true, censors NSFW images if accidentally generated

    @SerialName("workers")
    val workers: List<String>? = null, // List of up to 5 workers allowed to service this request

    @SerialName("worker_blacklist")
    val workerBlacklist: Boolean = false, // Treats the worker list as a blacklist if true

    @SerialName("models")
    val models: List<String>? = null, // Specify which models are allowed for the request

    @SerialName("source_image")
    val sourceImage: String? = null, // Base64-encoded webp image for img2img

    @SerialName("source_processing")
    val sourceProcessing: String = "img2img", // Specifies how to process the source image

    @SerialName("source_mask")
    val sourceMask: String? = null, // Base64-encoded mask for inpainting or outpainting

    @SerialName("extra_source_images")
    val extraSourceImages: List<ExtraSourceImage>? = null, // List of additional source images

    @SerialName("r2")
    val r2: Boolean = true, // Sends the image via Cloudflare R2 download link

    @SerialName("shared")
    val shared: Boolean = false, // Shares the image with LAION to reduce kudos cost

    @SerialName("replacement_filter")
    val replacementFilter: Boolean = true, // Sanitizes prompts through a replacement filter

    @SerialName("style")
    val style: String? = null, // The style to apply to the generation

    @SerialName("webhook")
    val webhook: String? = null, // URL for sending generation completion notifications

    @SerialName("dry_run")
    val dryRun: Boolean = false, // If true, estimates the cost without performing the request

    @SerialName("proxied_account")
    val proxiedAccount: String? = null, // Used to identify a proxy account if applicable

    @SerialName("disable_batching")
    val disableBatching: Boolean = false, // Disables batching for accurate seeds (restricted feature)

    @SerialName("allow_downgrade")
    val allowDowngrade: Boolean = false // Allows request downgrade in steps/resolution if kudos is insufficient
)
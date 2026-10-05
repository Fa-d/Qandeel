package dev.sadakat.qandeel.shared.designsystem.effects

/**
 * The sky, as one fragment shader: a vertical gradient, a slow nebula of noise, the lamp's warm glow
 * low in the frame, and three planes of twinkling star dust that move against each other as the
 * page scrolls (the far plane barely, the near one most), which is what reads as depth.
 *
 * Written in the subset of SkSL that AGSL (Android 13+) and Skia (iOS, desktop) both accept: float
 * literals everywhere, constant loop bounds, no arrays.
 */
internal const val SKY_SHADER = """
uniform float2 resolution;
uniform float time;
uniform float scroll;
uniform float starAmount;
uniform float4 skyTop;
uniform float4 skyBottom;
uniform float4 nebula;
uniform float4 glow;
uniform float4 star;
uniform float2 glowCenter;

float hash(float2 p) {
    p = fract(p * float2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

float noise(float2 p) {
    float2 i = floor(p);
    float2 f = fract(p);
    float a = hash(i);
    float b = hash(i + float2(1.0, 0.0));
    float c = hash(i + float2(0.0, 1.0));
    float d = hash(i + float2(1.0, 1.0));
    float2 u = f * f * (3.0 - 2.0 * f);
    return mix(mix(a, b, u.x), mix(c, d, u.x), u.y);
}

float fbm(float2 p) {
    float v = 0.0;
    float a = 0.5;
    for (int i = 0; i < 5; i++) {
        v += a * noise(p);
        p = p * 2.03 + float2(17.0, 9.0);
        a *= 0.5;
    }
    return v;
}

float stars(float2 uv, float scale, float seed) {
    float2 g = uv * scale;
    float2 id = floor(g);
    float2 f = fract(g) - 0.5;
    float h = hash(id + seed);
    float present = step(0.86, h);
    float2 offset = float2(hash(id + seed + 3.1), hash(id + seed + 7.7)) - 0.5;
    float d = length(f - offset * 0.7);
    float size = 0.018 + 0.04 * hash(id + seed + 1.3);
    float twinkle = 0.55 + 0.45 * sin(time * (0.6 + 2.2 * h) + h * 40.0);
    float core = smoothstep(size, 0.0, d);
    float halo = smoothstep(size * 3.0, 0.0, d) * 0.12;
    return present * (core + halo) * twinkle;
}

half4 main(float2 fragCoord) {
    float2 uv = fragCoord / resolution.y;
    float y = fragCoord.y / resolution.y;
    float3 color = mix(skyTop.rgb, skyBottom.rgb, smoothstep(0.0, 1.0, y));

    float2 drift = float2(time * 0.006, -scroll * 0.00008);
    float cloud = fbm(uv * 2.2 + drift);
    float wisps = smoothstep(0.45, 0.85, cloud) * (0.55 + 0.45 * fbm(uv * 5.0 - drift * 2.0));
    color += nebula.rgb * nebula.a * wisps;

    float2 toGlow = (fragCoord - glowCenter) / resolution.y;
    float warm = exp(-dot(toGlow, toGlow) * 16.0);
    color += glow.rgb * glow.a * warm;

    float s = 0.0;
    s += stars(uv + float2(0.0, scroll * 0.00004), 34.0, 1.0) * 0.55;
    s += stars(uv + float2(0.0, scroll * 0.00012), 22.0, 2.0) * 0.8;
    s += stars(uv + float2(0.0, scroll * 0.00030), 13.0, 3.0);
    color += star.rgb * star.a * s * starAmount;

    return half4(half3(color), 1.0);
}
"""

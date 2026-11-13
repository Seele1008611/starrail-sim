#version 150

uniform vec4 ColorModulator;
uniform float CombatTime;
uniform float EmissionPass;

in vec4 vertexColor;
in vec2 materialCoord;
out vec4 fragColor;

void main() {
    vec4 color = vertexColor * ColorModulator;
    // UV0.y has a four-unit band per material and a 0..1 transverse coordinate.
    // All vertices of a face occupy the same band; no extra draw calls or textures.
    float material = floor(materialCoord.y * 0.25);
    if (material < 0.5) {
        fragColor = color;
        return;
    }
    float across = materialCoord.y - material * 4.0;
    float edge = 1.0 - smoothstep(0.035, 0.24, min(across, 1.0 - across));
    float along = materialCoord.x;
    if (material < 1.5) {
        float flow = smoothstep(0.48, 0.98, sin(along * 7.0 + CombatTime * 12.0));
        float spine = 1.0 - smoothstep(0.025, 0.19, abs(across - 0.5));
        float chevron = smoothstep(0.72, 0.98, sin(along * 16.0 - abs(across - 0.5) * 9.0 + CombatTime * 17.0));
        color.rgb = mix(color.rgb * (0.78 + 0.22 * flow), vec3(0.72, 0.96, 1.0),
            min(0.72, 0.18 * flow + 0.28 * spine + 0.30 * chevron * edge));
        color.a *= 0.90 + 0.10 * max(flow, edge);
    } else if (material < 2.5) {
        float pulse = 0.5 + 0.5 * sin(along * 5.0 - CombatTime * 3.4);
        float bands = smoothstep(0.78, 0.97, sin(along * 15.0 - CombatTime * 4.0));
        color.rgb = mix(color.rgb * (0.88 + 0.12 * pulse), vec3(0.96, 0.76, 0.36), 0.18 * edge + 0.10 * bands);
        float seams = pow(0.5 + 0.5 * cos(along * 23.0 + floor(across * 6.0) * 1.57), 12.0);
        color.rgb = mix(color.rgb, vec3(1.0, 0.87, 0.50), 0.28 * seams * edge + 0.16 * bands);
        color.a *= 0.90 + 0.10 * pulse;
    } else if (material < 3.5) {
        float sap = smoothstep(0.35, 0.98, sin(along * 8.0 - CombatTime * 3.5));
        float vein = 1.0 - smoothstep(0.03, 0.16, abs(across - 0.5));
        color.rgb = mix(color.rgb * (0.90 + 0.10 * sap), vec3(0.84, 0.92, 0.64), 0.16 * sap * vein + 0.05 * edge);
        float branches = pow(0.5 + 0.5 * cos(along * 18.0 - abs(across - 0.5) * 24.0), 10.0);
        color.rgb = mix(color.rgb, vec3(0.94, 1.0, 0.70), 0.24 * sap * (branches + vein * 0.5));
        color.a *= 0.92 + 0.08 * sap;
    } else if (material < 4.5) {
        float heat = 0.5 + 0.5 * sin(along * 11.0 + CombatTime * 9.0 + sin(across * 18.0));
        color.rgb = mix(color.rgb * (0.88 + 0.12 * heat), vec3(1.0, 0.48, 0.20), 0.18 * heat + 0.12 * edge);
        float fissure = pow(0.5 + 0.5 * sin(along * 19.0 + sin(across * 29.0) * 2.0 - CombatTime * 12.0), 5.0);
        color.rgb = mix(color.rgb * (0.78 + 0.22 * heat), vec3(1.0, 0.87, 0.40), 0.42 * fissure * (0.4 + 0.6 * edge));
        color.a *= 0.90 + 0.10 * heat;
    } else if (material < 5.5) {
        float scan = smoothstep(0.82, 0.98, sin(along * 18.0 - CombatTime * 8.0));
        float calculation = 0.5 + 0.5 * sin(along * 6.0 + CombatTime * 2.5);
        color.rgb = mix(color.rgb * (0.87 + 0.13 * calculation), vec3(0.86, 0.62, 1.0), 0.16 + 0.26 * scan + 0.08 * edge);
        vec2 circuit = abs(fract(vec2(along * 4.0 - CombatTime * 0.5, across * 8.0)) - 0.5);
        float trace = 1.0 - smoothstep(0.025, 0.08, min(circuit.x, circuit.y));
        color.rgb = mix(color.rgb, vec3(0.95, 0.82, 1.0), 0.26 * trace * (0.4 + 0.6 * scan));
        color.a *= 0.88 + 0.12 * max(scan, calculation);
    } else if (material < 6.5) {
        float erosion = smoothstep(0.28, 0.98, sin(along * 9.0 - CombatTime * 5.0 + sin(across * 24.0)));
        float rim = 1.0 - smoothstep(0.01, 0.22, abs(across - 0.5));
        color.rgb = mix(color.rgb * (0.78 + 0.22 * erosion), vec3(0.48, 0.24, 0.82), 0.14 + 0.16 * rim);
        float fracture = smoothstep(0.65, 0.98, sin(along * 21.0 - CombatTime * 4.0 + across * 13.0));
        color.rgb = mix(color.rgb * (0.68 + 0.32 * edge), vec3(0.74, 0.40, 1.0), fracture * edge * 0.48);
        color.a *= 0.90 + 0.10 * erosion;
    } else if (material < 7.5) {
        float beat = 0.5 + 0.5 * sin(along * 4.5 - CombatTime * 3.14159);
        float note = smoothstep(0.86, 0.99, sin(along * 13.0 - CombatTime * 6.28318));
        color.rgb = mix(color.rgb * (0.91 + 0.09 * beat), vec3(1.0, 0.72, 0.88), 0.18 * beat + 0.12 * note);
        float overtone = pow(0.5 + 0.5 * sin(along * 14.0 + across * 9.0 - CombatTime * 6.28318), 6.0);
        color.rgb = mix(color.rgb, vec3(1.0, 0.88, 0.64), 0.30 * overtone * (0.5 + 0.5 * edge));
        color.a *= 0.92 + 0.08 * beat;
    } else if (material < 8.5) {
        float frost = 0.5 + 0.5 * sin(along * 8.0 - CombatTime * 2.2);
        float shard = smoothstep(0.84, 0.98, sin(along * 22.0 + across * 17.0));
        color.rgb = mix(color.rgb * (0.88 + 0.12 * frost), vec3(0.60, 0.91, 1.0), 0.18 * frost + 0.25 * shard);
        float facet = abs(fract(along * 3.0 + across * 2.0) * 2.0 - 1.0);
        color.rgb = mix(color.rgb * (0.78 + 0.22 * facet), vec3(0.85, 0.99, 1.0), 0.27 * pow(facet, 5.0) + 0.15 * edge);
        color.a *= 0.90 + 0.10 * frost;
    } else if (material < 9.5) {
        float bounce = 0.5 + 0.5 * sin(along * 9.0 + CombatTime * 7.0);
        float confetti = smoothstep(0.72, 0.98, sin(along * 17.0 + across * 21.0 + CombatTime * 5.0));
        vec3 party = mix(vec3(1.0, 0.34, 0.66), vec3(1.0, 0.83, 0.35), confetti);
        color.rgb = mix(color.rgb * (0.85 + 0.15 * bounce), party, 0.18 * bounce + 0.20 * confetti);
        float flip = step(0.5, fract(along * 2.0 + floor(CombatTime * 4.0) * 0.25 + across));
        color.rgb = mix(color.rgb, mix(vec3(1.0, 0.70, 0.88), vec3(1.0, 0.88, 0.52), flip), 0.22 * confetti);
        color.a *= 0.88 + 0.12 * max(bounce, confetti);
    }
    // Material ten: voidranger energy, kept separate from player Path palettes.
    if (material > 9.5 && material < 10.5) {
        float fracture = pow(0.5 + 0.5 * sin(along * 17.0 - CombatTime * 9.0 + across * 11.0), 7.0);
        float core = 1.0 - smoothstep(0.04, 0.34, abs(across - 0.5));
        color.rgb = mix(color.rgb * 0.80, vec3(0.86, 0.59, 1.0), 0.30 * fracture + 0.18 * core);
        color.a *= 0.83 + 0.17 * max(fracture, core);
    }
    if (material > 10.5) {
        float star = pow(0.5 + 0.5 * sin(along * 12.0 - CombatTime * 5.0 + across * 6.0), 9.0);
        float spine = 1.0 - smoothstep(0.04, 0.26, abs(across - 0.5));
        color.rgb = mix(color.rgb * 0.86, vec3(0.74, 1.0, 0.96), 0.28 * star + 0.15 * spine);
        color.a *= 0.85 + 0.15 * max(star, spine);
    }
    // A separate HDR target captures emission without brightening the whole world.
    if (EmissionPass > 0.5) {
        if (material < 0.5 || material > 11.5) discard;
        float energy = 1.0;
        if (material < 1.5) {
            energy = 3.4 + 1.8 * smoothstep(0.0, 1.0, sin(along * 9.0 + CombatTime * 14.0));
        } else if (material < 2.5) {
            float shieldPulse = 0.5 + 0.5 * sin(along * 10.0 - CombatTime * 5.5);
            color.rgb = mix(color.rgb, vec3(1.0, 0.72, 0.28), 0.16 + 0.14 * edge);
            energy = 2.8 + 1.5 * shieldPulse + 0.8 * edge;
        } else if (material < 3.5) {
            float lifePulse = 0.5 + 0.5 * sin(along * 7.0 - CombatTime * 3.2);
            color.rgb = mix(color.rgb, vec3(0.48, 1.0, 0.66), 0.14 + 0.10 * edge);
            energy = 2.25 + 1.15 * lifePulse + 0.55 * edge;
        } else if (material < 4.5) {
            float ember = 0.5 + 0.5 * sin(along * 13.0 + CombatTime * 11.0);
            color.rgb = mix(color.rgb, vec3(1.0, 0.34, 0.10), 0.22 + 0.16 * edge);
            energy = 3.0 + 1.4 * ember + 0.7 * edge;
        } else if (material < 5.5) {
            float solve = 0.5 + 0.5 * sin(along * 10.0 - CombatTime * 5.0);
            color.rgb = mix(color.rgb, vec3(0.74, 0.46, 1.0), 0.18 + 0.16 * edge);
            energy = 2.55 + 1.5 * solve + 0.65 * edge;
        } else if (material < 6.5) {
            float voidPulse = 0.5 + 0.5 * sin(along * 8.0 - CombatTime * 4.0);
            color.rgb = mix(color.rgb, vec3(0.62, 0.30, 1.0), 0.22 + 0.16 * edge);
            energy = 2.35 + 1.25 * voidPulse + 0.75 * edge;
        } else if (material < 7.5) {
            float chord = 0.5 + 0.5 * sin(along * 6.0 - CombatTime * 6.28318);
            color.rgb = mix(color.rgb, vec3(1.0, 0.56, 0.82), 0.18 + 0.16 * edge);
            energy = 2.45 + 1.2 * chord + 0.55 * edge;
        } else if (material < 8.5) {
            float ice = 0.5 + 0.5 * sin(along * 9.0 - CombatTime * 2.6);
            color.rgb = mix(color.rgb, vec3(0.48, 0.88, 1.0), 0.2 + 0.15 * edge);
            energy = 2.8 + 1.35 * ice + 0.8 * edge;
        } else if (material < 9.5) {
            float burst = 0.5 + 0.5 * sin(along * 11.0 + CombatTime * 8.0);
            color.rgb = mix(color.rgb, vec3(1.0, 0.48, 0.76), 0.2 + 0.16 * edge);
            energy = 2.8 + 1.6 * burst + 0.65 * edge;
        }
        if (material > 9.5) {
            energy = 2.3 + 1.2 * edge + 0.7 * pow(0.5 + 0.5 * sin(along * 17.0 - CombatTime * 9.0), 7.0);
        }
        if (material > 10.5) {
            energy = 2.4 + edge * 1.15 + 0.8 * pow(0.5 + 0.5 * sin(along * 12.0 - CombatTime * 5.0), 9.0);
        }
        color.rgb *= energy;
    }
    fragColor = color;
}

#pragma version(1)
#pragma rs java_package_name(org.flightofstairs.adirstat.view.drawing)
#pragma rs_fp_relaxed

typedef struct Surface {
    float x1;
    float x2;

    float y1;
    float y2;
} Surface_t;

const float Ia = 40;
const float Is = 215;
const float Lx = 0.09759;
const float Ly = 0.19518;
const float Lz = 0.9759;

const int a = 0xFF000000;

Surface_t s;
int left;
int top;

int r;
int g;
int b;

void root(uint32_t *v_out, const void *usrData, uint32_t x, uint32_t y) {
    float nx = -(2 * s.x2 * (x + left + 0.5) + s.x1);
    float ny = -(2 * s.y2 * (y + top + 0.5) + s.y1);
    float cosa = (nx * Lx + ny * Ly + Lz) * half_rsqrt((float) (nx * nx + ny * ny + 1.0));

    float intensity = (Ia + fmax((float) 0, Is * cosa)) / 256;

    *v_out = a
            | (int) (r * intensity) << 16
            | (int) (g * intensity) << 8
            | (int) (b * intensity);
}
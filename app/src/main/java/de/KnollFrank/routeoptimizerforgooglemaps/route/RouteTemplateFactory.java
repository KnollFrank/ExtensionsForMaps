package de.KnollFrank.routeoptimizerforgooglemaps.route;

import de.KnollFrank.routeoptimizerforgooglemaps.common.URLs;

public class RouteTemplateFactory {

    private static final Route ENTIRE_ROUTE_TEMPLATE =
            GoogleMapsRouteExtractor.extractRouteFromDirectionsUrl(
                    URLs.createUrl(
                            "https://www.google.com/maps/dir/Frauenplan+21/Villa+Kleine+Wartburg/Gastst%C3%A4tte+%26+Pension+%22Zum+Burschen%22/Lutherhaus+Eisenach/Kartoffelhaus+Eisenach/St.+Elisabeth/G%C3%B6bel's+Sophien+Hotel/Vienna+House+by+Wyndham+Th%C3%BCringer+Hof+Eisenach/Logop%C3%A4die+Sprechlaune+Julia+Zanev+%26+Elisabeth+G%C3%B6thling/Eisenach/G%C3%A4stehaus+Wohngut+Mario+Hirt/Staatliches+Berufsschulzentrum+%22Heinrich+Ehrhardt%22/Karl-Marx-Stra%C3%9Fe+34+Parking/Lidl/PTW-Palliativ+Team+Wartburgregion+GmbH/F.+Segura+Deutschland+GmbH/Netto+Marken-Discount/Max-Reger-Hof/Restaurant+%26+Cafe+Tino+Kuchinke/AWO+Kindergarten+%22Haus+Sonnenschein%22/REWE/TTM+Eisenach/Robert+Schramm+Reparaturservice/Innenausbau+Schwertfeger+GmbH/discovAIR/CARTEC+24/Diakonie+Seniorenzentrum+WARTBURGBLICK+Eisenach/data=!4m166!4m165!1m5!1m4!1s0x47a49c7e2c33a795:0xf94249802aa4933f!8m2!3d50.9714902!4d10.3224862!1m5!1m4!1s0x47a49c7d9d790c3b:0x307bd7031a10bc2f!8m2!3d50.966626299999994!4d10.3203263!1m5!1m4!1s0x47a49c83ec79f79d:0x4694f41d0089c9f6!8m2!3d50.969099!4d10.331197999999999!1m5!1m4!1s0x47a49c7f753b1873:0x3a9c8a94aac1c015!8m2!3d50.9733321!4d10.320159!1m5!1m4!1s0x47a49b804a3872b3:0xf74003abce73cb4a!8m2!3d50.9765203!4d10.3232226!1m5!1m4!1s0x47a49b80f1c243d1:0xa7ef095002a045e4!8m2!3d50.976238599999995!4d10.3206805!1m5!1m4!1s0x47a49b80f01a8693:0x62a6db49d7a92b20!8m2!3d50.976670299999995!4d10.320717499999999!1m5!1m4!1s0x47a49b8020a33f7b:0xa26bc6db34083941!8m2!3d50.9755975!4d10.324833199999999!1m5!1m4!1s0x47a49b81de9b889f:0x11bbfd3ad266a903!8m2!3d50.9778143!4d10.3253671!1m5!1m4!1s0x47a49b81e3833263:0x8047d0e2e218be94!8m2!3d50.9784958!4d10.3255314!1m5!1m4!1s0x47a49b7b6fc3a6a5:0x24360787e7c426eb!8m2!3d50.979115199999995!4d10.3375795!1m5!1m4!1s0x47a49b7a5196d4b1:0xaf192bd0493d4c98!8m2!3d50.981549799999996!4d10.3361237!1m5!1m4!1s0x47a49b8151b04a29:0x60ad1e78c388fac1!8m2!3d50.9793704!4d10.3197256!1m5!1m4!1s0x47a49b83c9c83a3d:0x9b6ab180f83c9033!8m2!3d50.9804393!4d10.3226898!1m5!1m4!1s0x47a49bb57245500f:0xac6369c2639552e2!8m2!3d50.9800756!4d10.3197726!1m5!1m4!1s0x47a49b8462195981:0x530e98964f964f4e!8m2!3d50.9813403!4d10.3187391!1m5!1m4!1s0x47a49b81868f766d:0x15ee561ca7fb8cfd!8m2!3d50.9806355!4d10.3155445!1m5!1m4!1s0x47a49b85a7c050fd:0x5cd24f11e9c9078d!8m2!3d50.9826069!4d10.3155121!1m5!1m4!1s0x47a49b8566188f3f:0xa989ea80130a0115!8m2!3d50.983402999999996!4d10.314488599999999!1m5!1m4!1s0x4171857409acb343:0x341fe5f6919aa035!8m2!3d50.983669!4d10.311442099999999!1m5!1m4!1s0x47a49b8f8bbbe5ed:0xce69daebd7a2a43c!8m2!3d50.9842231!4d10.3135405!1m5!1m4!1s0x47a49b8f7f74747f:0x223063cb1c1e087e!8m2!3d50.982982799999995!4d10.313579899999999!1m5!1m4!1s0x47a49b8f5a579aef:0x525823434a6ce813!8m2!3d50.9812678!4d10.3123766!1m5!1m4!1s0x47a49b88d4f57d1b:0xc9367acea0cf7081!8m2!3d50.9806526!4d10.3096525!1m5!1m4!1s0x47a49b2af04a0beb:0x2cf7f9b495c98e22!8m2!3d50.981265199999996!4d10.3082184!1m5!1m4!1s0x47a49b8bf8055555:0xaf3ea51c77925029!8m2!3d50.981142899999995!4d10.304991!1m5!1m4!1s0x47a49b8c467b9947:0xf945e8505ba986ea!8m2!3d50.98269!4d10.30293!2m1!2b1!3e0?utm_source=mstt_0&g_ep=CAESBzI2LjI4LjMYACCRQSqpASw5NDI2NzcyNyw5NDI5MjE5NSw5NDI5OTUzMiwxMDA3OTY0OTgsMTAwNzk3NzYxLDEwMDc5NjUzNSwxMjE3OTM1OTAsOTQyODA1NzYsMTAwODExOTYwLDk0MjA3Mzk0LDk0MjA3NTA2LDk0MjA4NTA2LDk0MjE4NjUzLDk0MjI5ODM5LDk0Mjc1MTY4LDk0Mjc5NjE5LDEwMDgxNTYzNSwxMDA4MjAyMzdCAkRF&skid=b9127731-5bc1-4b06-90ac-2474a353a98b"));

    public static Route createRouteTemplate(final int numberOfStops) {
        if (numberOfStops < 0 || numberOfStops > ENTIRE_ROUTE_TEMPLATE.stops().size()) {
            throw new IllegalArgumentException("" + numberOfStops);
        }
        return RouteFactory.createRoute(
                ENTIRE_ROUTE_TEMPLATE
                        .stops()
                        .subList(0, numberOfStops));
    }
}

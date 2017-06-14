// bintray settings
bintrayOrganization := Some("micronautics")
bintrayRepository := "scala"
bintrayPackageLabels := Seq("email", "scala")
bintrayVcsUrl := Some(s"git@github.com:mslinn/${ name.value }.git")

// sbt-site settings
enablePlugins(SiteScaladocPlugin)
siteSourceDirectory := target.value / "api"
publishSite

// sbt-ghpages settings
enablePlugins(GhpagesPlugin)
git.remoteRepo := s"git@github.com:mslinn/${ name.value }.git"

import { Fragment } from "react";
import { useTranslation } from "react-i18next";

type TabRenderersProps = {
  specificRenderers: string[];
  showRenderer: string;
  onRendererChange?: (renderer: string) => void;
};

const TabRenderers = ({ specificRenderers, showRenderer, onRendererChange }: TabRenderersProps) => {
  const isRendererAvailable = (renderer: string): boolean => {
    const availableRenderers = ["raw", "text dump", "xml", "xml dump", "tree", "hex", "hex dump", "json"];
    return availableRenderers.includes(renderer);
  };
  const { t } = useTranslation();

  const handleTabClick = (renderer: string) => {
    onRendererChange?.(renderer);
  };

  return (
    <div className="flex items-center gap-4">
      {specificRenderers?.map(
        (renderer, index) =>
          isRendererAvailable(renderer) && (
            <Fragment key={renderer}>
              <div className="relative group flex items-center">
                <button
                  id="content-tab-renderer"
                  key={renderer}
                  type="button"
                  title={t("gzl.message.capture.view_content_in") + " " + renderer + " " + t("gzl.message.capture.format")}
                  onClick={() => handleTabClick(renderer)}
                  className={`${
                    showRenderer === renderer ? "bg-lightblue" : ""
                  } enabled:hover:bg-lightpurple  py-1 px-4 rounded-xl enabled:cursor-pointer`}
                >
                  {renderer}
                </button>
              </div>
              {index < specificRenderers.length - 1 && <span>|</span>}
            </Fragment>
          )
      )}
    </div>
  );
};

export default TabRenderers;
